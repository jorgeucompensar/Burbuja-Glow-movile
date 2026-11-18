package com.example.burbujaglow

import com.example.burbujaglow.core.components.fmtCOP
import com.example.burbujaglow.data.model.ItemCarrito
import com.example.burbujaglow.data.model.ProductoComprador
import com.example.burbujaglow.data.model.totalesCarrito
import com.example.burbujaglow.feature.auth.AuthViewModel
import com.example.burbujaglow.feature.auth.LoginResult
import com.example.burbujaglow.feature.shop.ShopViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicaTest {

    private fun producto(id: Int, precio: Int, anterior: Int = 0, stock: Int = 10) =
        ProductoComprador(id, "P$id", "Labiales", "$precio", "desc", precio, "4.5", stock, "", anterior)

    @Test
    fun totales_subtotalEsSumaDePreciosActuales() {
        val carrito = listOf(ItemCarrito(producto(1, 5000, 6500), 2), ItemCarrito(producto(2, 12000), 1))
        val t = totalesCarrito(carrito)
        assertEquals(22000, t.subtotal)
        assertEquals(3000, t.descuento) // ahorro frente a precio anterior
        assertEquals(9900, t.envio) // < 150.000
        assertEquals(31900, t.total)
    }

    @Test
    fun totales_envioGratisDesde150000() {
        val carrito = listOf(ItemCarrito(producto(1, 80000), 2))
        val t = totalesCarrito(carrito)
        assertEquals(0, t.envio)
        assertEquals(160000, t.total)
    }

    @Test
    fun login_adminCorrecto() {
        val vm = AuthViewModel()
        vm.onUsuarioChange("admin"); vm.onPasswordChange("admin12345")
        assertTrue(vm.intentarLogin() is LoginResult.Admin)
    }

    @Test
    fun login_adminClaveIncorrecta() {
        val vm = AuthViewModel()
        vm.onUsuarioChange("admin"); vm.onPasswordChange("mala")
        assertTrue(vm.intentarLogin() is LoginResult.Error)
    }

    @Test
    fun login_comprador() {
        val vm = AuthViewModel()
        vm.onUsuarioChange("maria"); vm.onPasswordChange("cualquiera")
        assertTrue(vm.intentarLogin() is LoginResult.Comprador)
    }

    @Test
    fun login_vendedorCorrecto() {
        val vm = AuthViewModel()
        vm.onUsuarioChange("vendedor"); vm.onPasswordChange("vendedor12345")
        assertTrue(vm.intentarLogin() is LoginResult.Vendedor)
    }

    @Test
    fun login_vendedorClaveIncorrecta() {
        val vm = AuthViewModel()
        vm.onUsuarioChange("vendedor"); vm.onPasswordChange("mala")
        assertTrue(vm.intentarLogin() is LoginResult.Error)
    }

    @Test
    fun checkout_validaciones() {
        val vm = ShopViewModel()
        vm.onFullNameChange("Ana María"); vm.onPhoneChange("300 123 4567")
        vm.onEmailChange("ana@correo.com"); vm.onAddressChange("Calle 12 # 34-56")
        vm.onCityChange("Bogotá"); vm.onDepartmentChange("Cundinamarca"); vm.onPostalChange("110111")
        assertTrue(vm.validarCheckout())
    }

    @Test
    fun checkout_correoInvalido() {
        val vm = ShopViewModel()
        vm.onFullNameChange("Ana María"); vm.onPhoneChange("300 123 4567")
        vm.onEmailChange("sin-arroba"); vm.onAddressChange("Calle 12 # 34-56")
        vm.onCityChange("Bogotá"); vm.onDepartmentChange("Cundinamarca"); vm.onPostalChange("110111")
        assertTrue(!vm.validarCheckout())
        assertTrue(vm.uiState.value.erroresCheckout.contains("email"))
    }

    @Test
    fun formatoCOP() {
        assertEquals("$49.900 COP", fmtCOP(49900))
    }
}
