import org.junit.Test
import js2ts

import static org.junit.Assert.*;

class js2tsTest {

    @Test
    void shouldHaveTwoVariableAndOneFunctionInGlobalScope() {
        def defPhaseString = js2ts.parse('''
var a = 1;
var b = 2;

function fun() {
}

var c = fun;

var d = {
  d1: function () {},
  d2: 3,
  d3: fun
//  get d3: function () {}
}
''').defPhase.globals.toString()
        assertEquals defPhaseString,
                'globals: [a: [], b: [], function fun: [], c: [], d: [d1: [' +
                'function anonymous[11, 6]: []], d2: [], d3: []]]'
    }

    @Test
    void shouldHaveOneLiteralObjectInGlobalScope() {
        def globals = js2ts.parse('''
var d = {
  d1: function (d_arg1) {
    var d11 = 0;
  },
  d2: 3,
  get d3() {}
}
''').defPhase.globals

        assertEquals globals.toString(),
                'globals: [d: [d1: [function anonymous[3, 6]: [locals: [d_arg1: [], d11: []]]], d2: [], getd3: []]]'

    }

    @Test
    void shouldHaveOneClassInGlobalScope() {
        def globals = js2ts.parse('''
function Person() {
    this.firstName = "a";
    this.lastName = "b";
}

Person.prototype.fullName = function (fullNameA1) {
    this.middleName = 'b';
    var fullName1;
    return this.firstName + ' ' + this.lastName;
};

Person.doStatic = function (arg_ds1) {
    var ds1 = 3;
}
''').defPhase.globals

        assertEquals globals.toString(),
                'globals: [function Person: [publics: [firstName: [], lastName: [], fullName: [function anonymous' +
                '[7, 28]: [locals: [fullNameA1: [], fullName1: []]]], middleName: []],statics: [doStatic: [function ' +
                'anonymous[13, 18]: [locals: [arg_ds1: [], ds1: []]]]]]]'

    }
}
