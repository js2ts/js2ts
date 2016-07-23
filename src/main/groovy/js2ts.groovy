import groovy.transform.Field
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker

import java.nio.charset.StandardCharsets

String inputFile = null;
if (args.length > 0) inputFile = args[0];
InputStream is = System.in;
if (inputFile != null) {
    is = new FileInputStream(inputFile);
}

parseInputStream(is)

@Field
ParseTreeWalker walker = new ParseTreeWalker();

@Field
DefPhase defPhase = new DefPhase();

def parseInputStream(InputStream is) {
    ANTLRInputStream input = new ANTLRInputStream(is);
    ECMAScriptLexer lexer = new ECMAScriptLexer(input);
    def tokens = new CommonTokenStream(lexer);
    def parser = new ECMAScriptParser(tokens);
    parser.setBuildParseTree(true);
    ParseTree tree = parser.program();
// show tree in text form
//        System.out.println(tree.toStringTree(parser));

    walker.walk(defPhase, tree);
// create next phase and feed symbol table info from def to ref phase
    RefPhase ref = new RefPhase(defPhase.globals, defPhase.scopes);
    walker.walk(ref, tree);
}

static def parse(String input) {
    def j = new js2ts()
    j.parseInputStream(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
    return j
}

