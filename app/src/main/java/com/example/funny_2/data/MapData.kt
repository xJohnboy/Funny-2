package com.example.funny_2.data

data class MapData(
    val page_information : Page_information,
    val entities : ArrayList<Entities>
)

data class Entities (
    val id : String,
    val updated_at : String,
    val created_at : String,
    val store_code : Int,
    val store_name : String,
    val area : String,
    val full_address : String,
    val address : String,
    val address_en : String,
    val tambon : String,
    val tambon_en : String,
    val amphoe_en : String,
    val amphoe : String,
    val province : String,
    val province_en : String,
    val address_th : String,
    val tambon_th : String,
    val amphoe_th : String,
    val province_th : String,
    val zipcode : String,
    val distance : Double,
    val branch_type : Int,
    val latitude : Double,
    val longitude : Double

)

data class Page_information (
    val page : Int,
    val number_of_page : Int,
    val size : Int
)


