/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
class Scope {
    String name
    Scope enclosingScope // null if global (outermost) scope
    Map<String, Scope> childScopes = new LinkedHashMap<>()

    Scope(){name = 'globals'}
    Scope(String name) { this.name = name }
    Scope(String name, Scope enclosingScope) {
        this.name = name
        this.enclosingScope= enclosingScope
    }

    Scope resolve(String name) {
        Scope s = childScopes.get(name)
        if (s != null) return s
        // if not here, check any enclosing scope
        if (enclosingScope != null) return enclosingScope.resolve(name)
        return null // not found
    }

    void define(Scope sym) {
        childScopes.put(sym.name, sym)
    }

    String toString() {
        return "$name: ${childScopes.values()}"
    }
}
