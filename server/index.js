'use strict';

const http = require('http');
const crypto = require('crypto');
const fs = require('fs');

const PORT = Number(process.env.PORT || 3000);
const BASE_URL = (process.env.BASE_URL || '').replace(/\/$/, '');
const EPAYCO_PUBLIC_KEY = process.env.EPAYCO_PUBLIC_KEY || '';
const EPAYCO_PRIVATE_KEY = process.env.EPAYCO_PRIVATE_KEY || '';
const EPAYCO_P_KEY = process.env.EPAYCO_P_KEY || '';
const EPAYCO_CUST_ID_CLIENTE = process.env.EPAYCO_CUST_ID_CLIENTE || '';
const EPAYCO_TEST_MODE = (process.env.EPAYCO_TEST_MODE || 'true') === 'true';
const SERVICE_ACCOUNT_PATH = process.env.GOOGLE_SERVICE_ACCOUNT_JSON || process.env.GOOGLE_APPLICATION_CREDENTIALS || '';
const PROJECT_ID = process.env.FIREBASE_PROJECT_ID || '';
const APIFY_BASE = 'https://apify.epayco.co';

let googleToken = { value: '', expiresAt: 0 };
let firebaseProjectId = PROJECT_ID;
let epaycoTokenCache = { value: '', expiresAt: 0 };

/** Codifica texto u objetos en base64url para construir el JWT de la cuenta de servicio. */
function b64url(value) {
  return Buffer.from(typeof value === 'string' ? value : JSON.stringify(value)).toString('base64url');
}

/** Envía una respuesta JSON con cabeceras CORS; se usa en todos los endpoints de la API. */
function sendJson(res, status, body) {
  const data = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(data),
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type'
  });
  res.end(data);
}

/** Envía respuesta en texto plano, empleada por el webhook y por errores simples. */
function sendText(res, status, body) {
  res.writeHead(status, {
    'Content-Type': 'text/plain; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': '*'
  });
  res.end(body);
}

/** Lee el cuerpo de la petición con límite de 1 MB para evitar cargas abusivas. */
function readBody(req) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on('data', (chunk) => {
      size += chunk.length;
      if (size > 1024 * 1024) {
        reject(new Error('Cuerpo demasiado grande'));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
    req.on('error', reject);
  });
}

/** Interpreta JSON o formularios URL-encoded según el Content-Type recibido. */
function parseBody(raw, contentType) {
  if (!raw) return {};
  if ((contentType || '').includes('application/json')) return JSON.parse(raw);
  const params = new URLSearchParams(raw);
  return Object.fromEntries(params.entries());
}

/** Convierte un valor JS al formato de tipos de Firestore REST (string, number, map, array...). */
function toValue(value) {
  if (value === null || value === undefined) return { nullValue: null };
  if (typeof value === 'boolean') return { booleanValue: value };
  if (typeof value === 'number') return Number.isInteger(value) ? { integerValue: String(value) } : { doubleValue: value };
  if (typeof value === 'string') return { stringValue: value };
  if (Array.isArray(value)) return { arrayValue: { values: value.map(toValue) } };
  if (typeof value === 'object') return { mapValue: { fields: toFields(value) } };
  return { stringValue: String(value) };
}

/** Transforma un objeto JS en el mapa de `fields` que espera la API REST de Firestore. */
function toFields(obj) {
  return Object.fromEntries(Object.entries(obj).map(([key, value]) => [key, toValue(value)]));
}

/** Revierte un valor tipado de Firestore a un valor JS normal, con `null` por defecto. */
function fromValue(value) {
  if (!value || typeof value !== 'object') return null;
  if ('stringValue' in value) return value.stringValue;
  if ('integerValue' in value) return Number(value.integerValue);
  if ('doubleValue' in value) return Number(value.doubleValue);
  if ('booleanValue' in value) return value.booleanValue;
  if ('nullValue' in value) return null;
  if ('arrayValue' in value) return (value.arrayValue.values || []).map(fromValue);
  if ('mapValue' in value) return fromFields(value.mapValue.fields || {});
  if ('timestampValue' in value) return value.timestampValue;
  return null;
}

/** Convierte el mapa `fields` de Firestore en un objeto JS plano. */
function fromFields(fields) {
  return Object.fromEntries(Object.entries(fields || {}).map(([key, value]) => [key, fromValue(value)]));
}

/** Obtiene y cachea un access token OAuth2; firma un JWT RS256 con la cuenta de servicio. */
async function getGoogleToken() {
  if (googleToken.value && Date.now() < googleToken.expiresAt) return googleToken.value;
  if (!SERVICE_ACCOUNT_PATH) throw new Error('GOOGLE_SERVICE_ACCOUNT_JSON no configurado');
  const key = JSON.parse(fs.readFileSync(SERVICE_ACCOUNT_PATH, 'utf8'));
  if (!firebaseProjectId) firebaseProjectId = key.project_id;
  const now = Math.floor(Date.now() / 1000);
  const header = b64url({ alg: 'RS256', typ: 'JWT', kid: key.private_key_id });
  const payload = b64url({
    iss: key.client_email,
    scope: 'https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/firebase',
    aud: key.token_uri,
    exp: now + 3600,
    iat: now
  });
  const signingInput = `${header}.${payload}`;
  const signature = crypto.sign('RSA-SHA256', Buffer.from(signingInput), key.private_key).toString('base64url');
  const response = await fetch(key.token_uri, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion: `${signingInput}.${signature}`
    })
  });
  const text = await response.text();
  if (!response.ok) throw new Error(`Token Google ${response.status}: ${text}`);
  const data = JSON.parse(text);
  googleToken = { value: data.access_token, expiresAt: Date.now() + ((data.expires_in || 3600) - 120) * 1000 };
  return googleToken.value;
}

/** Cliente REST de Firestore: construye la URL, adjunta el Bearer y aplica timeout de 10 s. */
async function firestoreRequest(method, docPath, body) {
  const token = await getGoogleToken();
  const url = `https://firestore.googleapis.com/v1/projects/${firebaseProjectId}/databases/(default)/documents${docPath}`;
  const response = await fetch(url, {
    method,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: body ? JSON.stringify(body) : undefined,
    signal: AbortSignal.timeout(10_000)
  });
  const text = await response.text();
  if (!response.ok) throw new Error(`Firestore ${response.status}: ${text}`);
  return text ? JSON.parse(text) : {};
}

/** Lee un documento y devuelve `null` si no existe; otros errores se propagan. */
async function getDoc(collection, id) {
  try {
    const doc = await firestoreRequest('GET', `/${collection}/${encodeURIComponent(id)}`);
    return doc.fields ? fromFields(doc.fields) : null;
  } catch (error) {
    if (error.message.startsWith('Firestore 404')) return null;
    throw error;
  }
}

/** Crea o reemplaza por completo un documento de Firestore. */
async function setDoc(collection, id, obj) {
  await firestoreRequest('PATCH', `/${collection}/${encodeURIComponent(id)}`, { fields: toFields(obj) });
}

/** Actualiza solo los campos indicados mediante `updateMask`, sin borrar los demás. */
async function patchDoc(collection, id, obj) {
  const query = Object.keys(obj).map((key) => `updateMask.fieldPaths=${encodeURIComponent(key)}`).join('&');
  await firestoreRequest('PATCH', `/${collection}/${encodeURIComponent(id)}?${query}`, { fields: toFields(obj) });
}

/** Valida que las llaves públicas y privadas de ePayco estén disponibles antes de usarlas. */
function requireEpayco() {
  if (!EPAYCO_PUBLIC_KEY || !EPAYCO_PRIVATE_KEY) throw new Error('Faltan EPAYCO_PUBLIC_KEY o EPAYCO_PRIVATE_KEY');
}

/** Compara la firma del webhook en tiempo constante para evitar ataques de temporización. */
function firmaValida(firma, recibida) {
  if (!/^[0-9a-f]{64}$/.test(recibida)) return false;
  const esperada = Buffer.from(firma, 'hex');
  const entrada = Buffer.from(recibida, 'hex');
  return esperada.length === entrada.length && crypto.timingSafeEqual(esperada, entrada);
}

/** Obtiene el token de la API Apify de ePayco y lo cachea durante cinco minutos. */
async function epaycoToken() {
  if (epaycoTokenCache.value && Date.now() < epaycoTokenCache.expiresAt) return epaycoTokenCache.value;
  requireEpayco();
  const basic = Buffer.from(`${EPAYCO_PUBLIC_KEY}:${EPAYCO_PRIVATE_KEY}`).toString('base64');
  const response = await fetch(`${APIFY_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Basic ${basic}` },
    body: '{}',
    signal: AbortSignal.timeout(10_000)
  });
  const text = await response.text();
  if (!response.ok) throw new Error(`Login ePayco ${response.status}: ${text}`);
  const data = JSON.parse(text);
  const token = data.token || data.token_apify || (data.data && (data.data.token || data.data.token_apify));
  if (!token) throw new Error('ePayco no devolvió token');
  epaycoTokenCache = { value: token, expiresAt: Date.now() + 5 * 60 * 1000 };
  return token;
}

/** Resuelve la URL pública del backend, priorizando `BASE_URL` sobre el host de la petición. */
function publicBaseUrl(req) {
  if (BASE_URL) return BASE_URL;
  const host = req.headers.host || `localhost:${PORT}`;
  return `http://${host}`;
}

/** Calcula subtotal, envío gratis desde $150.000 y total del pedido. */
function calcularTotales(items, productos) {
  const subtotal = items.reduce((sum, item) => sum + Number(productos[item.productoId].precioValor) * Number(item.cantidad), 0);
  const envio = subtotal > 0 && subtotal < 150000 ? 9900 : 0;
  return { subtotal, envio, total: subtotal + envio };
}

/** Crea una sesión de checkout ePayco: valida productos y stock, guarda el pendiente y devuelve `sessionId`. */
async function handleSession(req, res) {
  const body = parseBody(await readBody(req), req.headers['content-type']);
  const pedidoId = String(body.pedidoId || '').trim();
  const items = Array.isArray(body.items) ? body.items : [];
  if (!pedidoId || items.length === 0) return sendJson(res, 400, { error: 'pedidoId e items son obligatorios' });

  const productos = {};
  const vendedorIds = [];
  const docs = await Promise.all(items.map((item) => getDoc('productos', String(item.productoId || '').trim())));
  for (let index = 0; index < items.length; index++) {
    const productoId = String(items[index].productoId || '').trim();
    const cantidad = Number(items[index].cantidad || 0);
    if (!productoId || cantidad <= 0) return sendJson(res, 400, { error: 'Item inválido' });
    const producto = docs[index];
    if (!producto) return sendJson(res, 404, { error: `Producto no encontrado: ${productoId}` });
    if (Number(producto.stock || 0) < cantidad) return sendJson(res, 409, { error: `Stock insuficiente en ${producto.nombre}` });
    productos[productoId] = { ...producto, cantidad };
    if (producto.vendedorId && !vendedorIds.includes(producto.vendedorId)) vendedorIds.push(producto.vendedorId);
  }

  const { total } = calcularTotales(items, productos);
  const cliente = body.cliente || {};
  const baseUrl = publicBaseUrl(req);
  const pending = {
    numero: pedidoId,
    cliente: String(cliente.nombre || ''),
    correo: String(cliente.email || ''),
    telefono: String(cliente.telefono || ''),
    fecha: new Date().toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' }),
    productos: items.map((item) => `${productos[item.productoId].nombre} x${item.cantidad}`).join(' · '),
    items: items.map((item) => ({
      productoId: String(item.productoId),
      cantidad: Number(item.cantidad),
      nombre: String(productos[item.productoId].nombre || ''),
      precioValor: Number(productos[item.productoId].precioValor || 0)
    })),
    total,
    pago: 'ePayco',
    estado: 'Pendiente',
    uid: pedidoId,
    compradorUid: String(body.compradorUid || ''),
    vendedorIds,
    direccion: String(cliente.direccion || ''),
    ciudad: String(cliente.ciudad || ''),
    departamento: String(cliente.departamento || ''),
    codigoPostal: String(cliente.codigoPostal || ''),
    latitud: Number(cliente.latitud || 0),
    longitud: Number(cliente.longitud || 0),
    estadoPago: 'PENDIENTE'
  };
  await setDoc('pagos_pendientes', pedidoId, pending);

  const token = await epaycoToken();
  const response = await fetch(`${APIFY_BASE}/payment/session/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({
      checkout_version: '2',
      name: 'Burbuja Glow',
      description: `Pedido ${pedidoId}`,
      amount: total,
      currency: 'COP',
      country: 'CO',
      lang: 'ES',
      invoice: pedidoId,
      test: EPAYCO_TEST_MODE,
      confirmation: `${baseUrl}/api/payments/epayco/confirmation`,
      response: `${baseUrl}/api/payments/epayco/response`,
      extras: { extra1: pedidoId, extra2: pending.compradorUid },
      billing: {
        name: pending.cliente,
        email: pending.correo,
        address: pending.direccion,
        mobilePhone: pending.telefono
      }
    })
  });
  const text = await response.text();
  if (!response.ok) throw new Error(`Crear sesión ePayco ${response.status}: ${text}`);
  const data = JSON.parse(text);
  const sessionId = data.data && (data.data.sessionId || data.data.session_id) || data.sessionId || data.session_id;
  if (!sessionId) throw new Error('ePayco no devolvió sessionId');
  return sendJson(res, 200, { sessionId, pedidoId, amount: total });
}

/** Descuenta stock y acumula vendidos por cada ítem después de confirmar un pago aprobado. */
async function descontarStock(items) {
  for (const item of items) {
    const productoId = String(item.productoId || '');
    const cantidad = Number(item.cantidad || 0);
    if (!productoId || cantidad <= 0) continue;
    const producto = await getDoc('productos', productoId);
    if (!producto) continue;
    const stockActual = Number(producto.stock || 0);
    await patchDoc('productos', productoId, {
      stock: Math.max(0, stockActual - cantidad),
      vendidos: Number(producto.vendidos || 0) + cantidad
    });
  }
}

/** Convierte un pago pendiente en pedido confirmado o cancelado y aplica el stock solo si se aprobó. */
async function procesarResultado(pedidoId, estadoPago, info) {
  const pending = await getDoc('pagos_pendientes', pedidoId);
  if (!pending) return;
  const aprobado = estadoPago === 'APROBADO';
  await patchDoc('pagos_pendientes', pedidoId, {
    estadoPago,
    refPayco: String(info.refPayco || ''),
    transactionId: String(info.transactionId || ''),
    respuesta: String(info.respuesta || '')
  });
  const pedido = {
    numero: pedidoId,
    cliente: pending.cliente || '',
    correo: pending.correo || '',
    telefono: pending.telefono || '',
    fecha: pending.fecha || '',
    productos: pending.productos || '',
    items: pending.items || [],
    total: Number(pending.total || 0),
    pago: 'ePayco',
    estado: aprobado ? 'Confirmado' : 'Cancelado',
    uid: pedidoId,
    compradorUid: pending.compradorUid || '',
    vendedorIds: pending.vendedorIds || [],
    latitud: Number(pending.latitud || 0),
    longitud: Number(pending.longitud || 0),
    estadoPago,
    direccion: pending.direccion || '',
    ciudad: pending.ciudad || '',
    departamento: pending.departamento || '',
    codigoPostal: pending.codigoPostal || '',
    refPayco: String(info.refPayco || ''),
    transactionId: String(info.transactionId || ''),
    respuesta: String(info.respuesta || '')
  };
  await setDoc('pedidos', pedidoId, pedido);
  if (aprobado) await descontarStock(pending.items || []);
}

/** Webhook de ePayco: valida firma, evita reprocesar transacciones y registra el resultado del pago. */
async function handleConfirmation(req, res) {
  const body = parseBody(await readBody(req), req.headers['content-type']);
  if (!EPAYCO_P_KEY || !EPAYCO_CUST_ID_CLIENTE) return sendText(res, 500, 'Webhook no configurado');
  const refPayco = String(body.x_ref_payco || body.ref_payco || '');
  const transactionId = String(body.x_transaction_id || '');
  const amount = String(body.x_amount || body.x_amount_ok || '');
  const currency = String(body.x_currency_code || 'COP');
  const firma = crypto.createHash('sha256')
    .update(`${EPAYCO_CUST_ID_CLIENTE}^${EPAYCO_P_KEY}^${refPayco}^${transactionId}^${amount}^${currency}`)
    .digest('hex');
  const recibida = String(body.x_signature || '').toLowerCase();
  if (!firmaValida(firma, recibida)) return sendText(res, 400, 'Firma inválida');

  const procesado = await getDoc('pagos', transactionId || 'sin-transaccion');
  if (procesado) return sendText(res, 200, 'OK');

  const pedidoId = String(body.x_extra1 || body.x_id_invoice || '');
  if (!pedidoId) return sendText(res, 400, 'Pedido no identificado');

  const codigo = String(body.x_cod_respuesta || '');
  const respuesta = String(body.x_response || '').toLowerCase();
  let estadoPago = 'PENDIENTE';
  if (codigo === '1' || respuesta.includes('aceptada')) estadoPago = 'APROBADO';
  else if (codigo === '2' || respuesta.includes('rechazada') || respuesta.includes('fallida')) estadoPago = 'RECHAZADO';

  await setDoc('pagos', transactionId, {
    pedidoId,
    estadoPago,
    refPayco,
    transactionId,
    amount,
    currency,
    respuesta: String(body.x_response_reason_text || body.x_response || ''),
    procesadoEn: new Date().toISOString()
  });
  await procesarResultado(pedidoId, estadoPago, {
    refPayco,
    transactionId,
    respuesta: String(body.x_response_reason_text || body.x_response || '')
  });
  return sendText(res, 200, 'OK');
}

/** Consulta el estado real del pedido o del pago pendiente para que la app haga polling. */
async function handleVerify(req, res, url) {
  const pedidoId = String(url.searchParams.get('pedidoId') || '').trim();
  if (!pedidoId) return sendJson(res, 400, { error: 'pedidoId obligatorio' });
  const pedido = await getDoc('pedidos', pedidoId);
  if (pedido) {
    return sendJson(res, 200, {
      numero: pedido.numero || pedidoId,
      estado: pedido.estado || '',
      estadoPago: pedido.estadoPago || 'PENDIENTE',
      respuesta: pedido.respuesta || ''
    });
  }
  const pending = await getDoc('pagos_pendientes', pedidoId);
  return sendJson(res, 200, {
    numero: pedidoId,
    estado: pending ? 'Pendiente' : 'No encontrado',
    estadoPago: pending ? pending.estadoPago || 'PENDIENTE' : 'NO_ENCONTRADO',
    respuesta: pending ? pending.respuesta || '' : ''
  });
}

/** Reenvía a ePayco la validación de una referencia de pago usada como respuesta del checkout. */
async function handleResponse(req, res, url) {
  const ref = String(url.searchParams.get('ref_payco') || '').trim();
  if (!ref) return sendJson(res, 400, { error: 'ref_payco obligatorio' });
  const response = await fetch(`https://secure.epayco.co/validation/v1/reference/${encodeURIComponent(ref)}`, {
    signal: AbortSignal.timeout(10_000)
  });
  const text = await response.text();
  if (!response.ok) return sendText(res, response.status, text);
  return sendJson(res, 200, JSON.parse(text));
}

/** Enrutador HTTP: health, creación de sesión, webhook, verificación y respuesta de ePayco. */
const server = http.createServer(async (req, res) => {
  const url = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
  if (req.method === 'OPTIONS') return sendText(res, 204, '');
  try {
    if (url.pathname === '/health') return sendJson(res, 200, { ok: true });
    if (url.pathname === '/api/payments/session' && req.method === 'POST') return await handleSession(req, res);
    if (url.pathname === '/api/payments/epayco/confirmation' && req.method === 'POST') return await handleConfirmation(req, res);
    if (url.pathname === '/api/payments/verify' && req.method === 'GET') return await handleVerify(req, res, url);
    if (url.pathname === '/api/payments/epayco/response' && req.method === 'GET') return await handleResponse(req, res, url);
    return sendJson(res, 404, { error: 'Ruta no encontrada' });
  } catch (error) {
    console.error(error.message);
    return sendJson(res, 500, { error: error.message });
  }
});

server.listen(PORT, () => {
  console.log(`Burbuja Glow payments backend en puerto ${PORT}`);
});
