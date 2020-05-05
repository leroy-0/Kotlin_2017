package koak

class Block(_name : String?, _type : ParseEnum) {
    private val config : Param = Param(_name, _type)
    private var retType : ParseEnum = ParseEnum.UNDEFINED
    private val parameters : MutableList<Param> = mutableListOf()
    private val components : MutableList<Component> = mutableListOf()

    fun print() {
        config.print()
        println("return : $retType")
        print("parameters :")
        for (param in parameters) {
            param.print()
        }
        println("Components:")
        for (component in components) {
            component.print()
        }
        println()
    }

    fun setRetType(_retType : ParseEnum) {
        retType = _retType
    }

    fun addParameter(name : String?, type : ParseEnum) {
        parameters.add(Param(name, type))
    }

    fun addComponent(component : Component) {
        components.add(component)
    }

    fun getConfig() : Param {
        return (config)
    }

    fun getReturnType() : ParseEnum {
        return (retType)
    }

    fun getParameters() : MutableList<Param> {
        return (parameters)
    }

    fun getComponents() : MutableList<Component> {
        return (components)
    }
}