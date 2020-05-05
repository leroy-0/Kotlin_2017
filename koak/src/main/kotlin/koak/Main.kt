package koak

class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val koak = Koak()
            if (args.size == 1 && args[0] == "debug")
                koak.run(true)
            else if (args.size == 1)
                koak.run(args[0])
            else
                koak.run(false)
        }
    }
}

