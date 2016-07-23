/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
 ***/
public class FunctionScope extends Scope {
    Map<String, Scope> publics = new LinkedHashMap<>();
    Map<String, Scope> statics = new LinkedHashMap<>();

    public FunctionScope(String name, Scope enclosingScope) {
        super(name, enclosingScope);
    }

//    public Scope resolve(String name) {
//        Scope s = arguments.get(name);
//        if (s != null) return s;
//        // if not here, check any enclosing scope
//        if (enclosingScope != null) {
//            return enclosingScope.resolve(name);
//        }
//        return null; // not found
//    }

//    public void define(Scope sym) {
//        arguments.put(sym.name, sym);
//    }

    public String toString() {
        def localsStr = childScopes.isEmpty() ? null : "locals: ${childScopes.values()}"
        def publicsStr = publics.isEmpty() ? null : "publics: ${publics.values()}"
        def staticsStr = statics.isEmpty() ? null : "statics: ${statics.values()}"
        "function $name: [${[localsStr, publicsStr, staticsStr].findAll {it != null}.join(',')}]"
    }
}
