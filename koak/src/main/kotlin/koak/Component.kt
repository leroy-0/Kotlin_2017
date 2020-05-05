package koak

class Component {
    private var tokenParse : ParseEnum? = null
    private var valueLeft : Param? = null
    private var tokenEnum : TokenEnum? = null
    private var valueRight : Param? = null
    private var children : MutableList<Component> = mutableListOf()

    fun print() {
        print("Left:")
        valueLeft?.print()
        print("Right:")
        valueRight?.print()
        println("tokenParse : ${tokenParse?.name}, tokenEnum : $tokenEnum, children: [")
        for (component in children) {
            component.print()
        }
        println("]")
    }

    fun addChild(component: Component) {
        children.add(component)
    }

    fun getChildren() : MutableList<Component> {
        return (children)
    }

    fun setValueLeft(value : Param?) {
        valueLeft = value
    }

    fun setValueRight(value : Param?) {
        valueRight = value
    }

    fun setTokenEnum(value : TokenEnum?) {
        tokenEnum = value
    }

    fun setParseEnum(value : ParseEnum?) {
        tokenParse = value
    }

    fun getValueLeft() : Param? {
        return (valueLeft)
    }

    fun getValueRight() : Param? {
        return (valueRight)
    }

    fun getTokenEnum() : TokenEnum? {
        return (tokenEnum)
    }

    fun getParseToken() : ParseEnum? {
        return (tokenParse)
    }

}