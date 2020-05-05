package koak

class Node {
    private var token : String?
    private var left : Node?
    private var right : Node?

    constructor() {
        this.token = null
        this.left = null
        this.right = null
    }

    constructor(token: String) {
        this.token = token
        this.left = null
        this.right = null
    }

    constructor(token: String, leftNode : Node, rightNode : Node) {
        this.token = token
        this.left = leftNode
        this.right = rightNode
    }

    fun print() {
        print(this.token)
        if (this.left == null && this.right == null) {

        }
        else {
            print(" Left: [")
            if (this.left != null) this.left?.print() else print(null)
            print("]")
            print(" Right: [")
            if (this.right != null) this.right?.print() else print(null)
            print("]")
            println()
        }
    }

    fun addNode(node : Node?, where : Boolean) {
        if (where)
            this.right = node
        else
            this.left = node
    }

    fun removeNode(where : Boolean) {
        if (where) {
            this.right = null
        }
        else {
            this.left = null
        }
    }

    fun setToken(str : String) {
        this.token = str
    }

    fun getToken() : String? {
        return (this.token)
    }

    fun getLeft() : Node? {
        return (this.left)
    }

    fun getRight() : Node? {
        return (this.right)
    }
}