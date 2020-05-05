package koak

class Param(_name : String?, _type : ParseEnum) {
    private val name : String? = _name
    private val type : ParseEnum = _type

    fun print() {
        println("name : $name, type : ${type.name}")
    }

    fun getName() : String {
        return (name.toString())
    }

    fun getType() : ParseEnum {
        return (type)
    }
}