import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTreeProperty

/**
 * Created by luis on 7/14/16.
 */
class RefPhase extends ECMAScriptBaseListener {

    ParseTreeProperty<Scope> scopes
    Scope globals
    Scope currentScope

    RefPhase(Scope globals, ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes
        this.globals = globals
    }

    @Override
    void enterProgram(ECMAScriptParser.ProgramContext ctx) {
        currentScope = globals
    }

    @Override
    void enterFunctionDeclaration(ECMAScriptParser.FunctionDeclarationContext ctx) {
        currentScope = scopes.get(ctx)
    }

    @Override
    void exitFunctionDeclaration(ECMAScriptParser.FunctionDeclarationContext ctx) {
        currentScope = currentScope.enclosingScope
    }

    @Override
    void enterBlock(ECMAScriptParser.BlockContext ctx) {
        currentScope = scopes.get(ctx)
    }

    @Override
    void exitBlock(ECMAScriptParser.BlockContext ctx) {
        currentScope = currentScope.enclosingScope
    }

    @Override
    void exitVariableDeclaration(ECMAScriptParser.VariableDeclarationContext ctx) {
        def name = ctx.Identifier().symbol.text
        def var = currentScope.resolve(name)

        if(var == null) {
            error(ctx.Identifier().symbol, "no such variable: $name")
        }
        if(var instanceof FunctionScope) {
            error(ctx.Identifier().symbol, "$name is not a variable")
        }
    }


    private void error(Token t, String msg) {
        printf("line %d:%d %s\n", t.getLine(), t.getCharPositionInLine(), msg);
    }
}

