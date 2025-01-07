package com.xrandolphx.elegantcommerce.data

sealed class Category(val category: String) {
    object camisa : Category("camisa")
    object pantalon : Category("pantalon")
    object zapatos : Category("zapato")
    object corbata : Category("corbata")
    object terno : Category("terno")
}