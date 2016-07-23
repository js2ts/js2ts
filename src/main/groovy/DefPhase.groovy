import ECMAScriptParser.FunctionDeclarationContext
import ECMAScriptParser.FunctionExpressionContext
import ECMAScriptParser.MemberDotExpressionContext
import ECMAScriptParser.IdentifierExpressionContext
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.ParseTreeProperty

class DefPhase extends ECMAScriptBaseListener {
    ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>()
    Scope globals
    Scope currentScope
    boolean insidePublicFunction

    @Override
    void enterProgram(ECMAScriptParser.ProgramContext ctx) {
        globals = new Scope()
        currentScope = globals
    }

    @Override
    void exitProgram(ECMAScriptParser.ProgramContext ctx) {
        println globals
    }

    private void saveScope(ParserRuleContext ctx, Scope subScope) {
        currentScope.define(subScope)
        scopes.put(ctx, subScope)
        currentScope = subScope
    }

    private void popScope() {
//        println currentScope
        currentScope = currentScope.enclosingScope
    }

    @Override
    void enterFunctionDeclaration(FunctionDeclarationContext ctx) {
        saveScope(ctx, new FunctionScope(ctx.Identifier().text, currentScope))
    }

    @Override
    void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        popScope()
    }

//    @Override
//    void enterArgumentsExpression(ECMAScriptParser.ArgumentsExpressionContext name) {
//        def se = name.singleExpression()
//        if(se instanceof ECMAScriptParser.IdentifierExpressionContext
//                && se.Identifier().text.isEmpty()
//                && name.arguments().argumentList().isEmpty())
//            saveScope(name)
//    }
//
//    @Override
//    void exitArgumentsExpression(ECMAScriptParser.ArgumentsExpressionContext name) {
//        def se = name.singleExpression()
//        if(se instanceof ECMAScriptParser.IdentifierExpressionContext
//                && se.Identifier().text.isEmpty()
//                && name.arguments().argumentList().isEmpty())
//            popScope()
//    }

    @Override
    void enterFunctionExpression(FunctionExpressionContext ctx) {
        saveScope(ctx, new FunctionScope(
                ctx.Identifier()?.text ?: "anonymous[$ctx.start.line, $ctx.start.charPositionInLine]",
                currentScope))
    }

    @Override
    void exitFunctionExpression(FunctionExpressionContext ctx) {
        popScope()
    }

    @Override
    public void enterBlock(ECMAScriptParser.BlockContext ctx) {
        // push new local scope
        currentScope = new Scope("block [$ctx.start.line, $ctx.start.charPositionInLine]", currentScope)
        scopes.put(ctx, currentScope)
    }

    @Override
    public void exitBlock(ECMAScriptParser.BlockContext ctx) {
        popScope()
    }

    @Override
    void enterPropertyExpressionAssignment(ECMAScriptParser.PropertyExpressionAssignmentContext ctx) {
        saveScope(ctx, new Scope(ctx.propertyName().text, currentScope))
    }

    @Override
    void exitPropertyExpressionAssignment(ECMAScriptParser.PropertyExpressionAssignmentContext ctx) {
        popScope()
    }

    @Override
    void enterPropertyGetter(ECMAScriptParser.PropertyGetterContext ctx) {
        saveScope(ctx, new Scope(ctx.getter().text, currentScope))
    }

    @Override
    void exitPropertyGetter(ECMAScriptParser.PropertyGetterContext ctx) {
        popScope()
    }

    @Override
    void exitFormalParameterList(ECMAScriptParser.FormalParameterListContext ctx) {
        ctx.Identifier().each {
            defineVar(it.symbol)
        }
    }

    @Override
    void enterAssignmentExpression(ECMAScriptParser.AssignmentExpressionContext ctx) {
        def leftExpression = ctx.singleExpression(0)
        def rightExpression = ctx.singleExpression(1)
        if (leftExpression instanceof MemberDotExpressionContext) {

            Scope subScope
            if (leftExpression.text.startsWith('this.')) {
                subScope = new Scope(leftExpression.identifierName().text, currentScope)
                if(insidePublicFunction) {
                    (currentScope.enclosingScope.enclosingScope as FunctionScope).publics.put(subScope.name, subScope)
                } else {
                    (currentScope as FunctionScope).publics.put(subScope.name, subScope)
                }
            } else if (leftExpression.text.contains('.prototype.')) {
                if(rightExpression instanceof FunctionExpressionContext) insidePublicFunction = true
                def className = (leftExpression.singleExpression() as MemberDotExpressionContext).singleExpression().text
                currentScope = currentScope.resolve(className)
                subScope = new Scope(leftExpression.identifierName().text, currentScope)
                (currentScope as FunctionScope).publics.put(subScope.name, subScope)
            } else if (leftExpression.singleExpression() instanceof IdentifierExpressionContext) {
                def className = leftExpression.singleExpression().text
                currentScope = currentScope.resolve(className)
                subScope = new Scope(leftExpression.identifierName().text, currentScope)
                (currentScope as FunctionScope).statics.put(subScope.name, subScope)
            } else {
                return
            }

            scopes.put(ctx, subScope)
            currentScope = subScope
        }
    }

    @Override
    void exitAssignmentExpression(ECMAScriptParser.AssignmentExpressionContext ctx) {
        def leftExpression = ctx.singleExpression(0)
        def rightExpression = ctx.singleExpression(1)
        if (leftExpression instanceof MemberDotExpressionContext
                && (leftExpression.text.startsWith('this.')
                    || leftExpression.text.contains('.prototype.')
                    || leftExpression.singleExpression() instanceof IdentifierExpressionContext))
            popScope()

        if (leftExpression instanceof MemberDotExpressionContext
                && leftExpression.text.contains('.prototype.')
                && rightExpression instanceof FunctionExpressionContext)
            insidePublicFunction = false
    }

    @Override
    void enterVariableDeclaration(ECMAScriptParser.VariableDeclarationContext ctx) {
        if (ctx.initialiser() != null
                && (ctx.initialiser().singleExpression() instanceof ECMAScriptParser.ObjectLiteralExpressionContext)) {
            saveScope(ctx, new Scope(ctx.Identifier().text, currentScope))
        }
    }

    @Override
    void exitVariableDeclaration(ECMAScriptParser.VariableDeclarationContext ctx) {
        if (ctx.initialiser() != null
                && (ctx.initialiser().singleExpression() instanceof ECMAScriptParser.ObjectLiteralExpressionContext)) {
            popScope()
        } else {
            defineVar(ctx.Identifier().symbol)
        }
    }

    protected void defineVar(Token node) {
        def var = new VariableScope(node.text)
        currentScope.define(var)
    }
}
