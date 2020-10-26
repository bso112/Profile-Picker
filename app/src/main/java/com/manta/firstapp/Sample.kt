package com.manta.firstapp

data class CloneTest(var a: Int, var arr: ArrayList<Int>) {
    constructor(other: CloneTest) : this(other.a, ArrayList(other.arr))
}


class MyData(val index : Int, val value : String)
fun main() {


    val maptest = hashMapOf(Pair(11, 11))
    maptest.put(11, 10) //덮어씀

    println(maptest[11])


    val set = hashSetOf<Int>()
    println( set.toString())

    val sdfsdf = arrayListOf("adsf", 22, "dasd")
    println(sdfsdf.toString())


    val arr = arrayOf("sad")
    arr[0] = "asdfff" //가능

    val list1 = listOf("asd", "ㄴㅁㅇ") //변경불가능한 arrayList를 만드는듯(추측)
    //list1[0] = "asdasd" 불가, add도 없음

    val list2 = arrayListOf("asd", ":sd")
    list2[0] = "asd"
    list2.add("asddd")

    //문자열과 숫자 바인딩 (문자열로 숫자를 찾고, 숫자로 문자열를 찾는다)
    //1. 배열 (배열이 크지 않은경우)
    val category = arrayListOf("a","b","c")
    category[0] // "a"
    category.indexOf("a") // 0

    //2. 객체
    val category_selfy = MyData(0, "a")
    category_selfy.index // 0
    category_selfy.value // a





//[(가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9)]{3,10}
    val mathResult = Regex("""[(가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9)]+""").matchEntire("assdㅁㄴㄴㅇㄹㄴㅇㄹ")
    System.out.println(mathResult?.value)



    var likes = arrayListOf<Int>()
    for(i in 0..5)
        likes.add(i)

    System.out.println(likes.toString())


    //for문 순회순서 테스트
    for (i in 0..10)
    {
        var msg = i.toString()
        System.out.println(msg)
    }
    //복사생성자를 이용한 깊은복사
    var origin = CloneTest(3, arrayListOf(3, 4))
    var copy = CloneTest(origin)
    copy.a = 5
    copy.arr.clear()

    //람다
    // * 람다는 중괄호에 싸여있다. 인자로 넘길때도 소괄호에 싸는게 아니라 중괄호에 싸야함.
    //인자로 람다받기
    callbackTest { a -> a + 6 } //9
    callbackTest { it + 6 } // 9. it은 인자가 하나일때 대신 쓸수있음.

    //확장함수
    //확장함수를 가진 함수객체 호출
    val obj = TestClass()
    obj.extendFunObj(3)

    val str = "manta said "
    println(str.pizzaisGreate()) // manta said pizza is greate
    println(extendString(27, "manta")) //"my name is manta, and i'm 27"

    //인자로 TestClass의 확장함수를 람다로 받기.
    extendFun { a: Int -> a + 3 }; //6


    /*
    데이터클래스
    데이터 보관용 클래스, 그 데이터를 처리하는 흔히 쓰이는 함수들의 집합(보일러플레이트)으로 이루어지는 클래스
    */
    val human = DataClass("manta", 0, 27)
    // Copy 는 객체 복사시에 특정 프로퍼티만 수정해서 복사하고싶을때 씀
    val olderMan = human.copy(age = 28);



    println(human) // class 를 data class로 만들면 toString(), equals(), hashCode() 등을 만들어줌

    //Object

    //comanion object
    println(printAge())

    //익명객체, 익명클래스
    createAnimal()

}

//함수
fun sum(a: Int, b: Int): Int {
    return a + b
}

fun sum2(a: Int, b: Int): Int = a + b
// fun sum3(a:Int, b:Int) :Int = return a+b 오류!

//switch
fun check(a: Int): Int {
    when (a) {
        3 -> println("low")
        10 -> println("heigh")
    }

    var num = when (a) {
        3 -> 4
        4 -> 5
        else -> 6
    }

    /** Multiplies this value by the other value. */
    //public operator fun times(other: Byte): Int


    return num


}


//@ 람다

//람다는 중괄호에 싸여져있다.
val foo = { num: Int, num2: Int -> num * num * num2 }

//람다형식(lambda type)
//https://kotlinlang.org/docs/reference/lambdas.html

//람다의 형식을 지정해줘서 람다를 인자로 받을 수 있다. 람다형식의 리턴값은 생략될 수 없다.
fun threeSum(value: Int, callback: (foo: Int, bar: Int) -> Int): Int {
    return value + callback(value, value)
}

fun callbackTest(callback: (arg: Int) -> Int) {
    println(callback(3))
}

//인자에 타입만적어도 됨.
fun callbackTest2(callback: (Int) -> Int) {
    println(callback(3))
}


// @ 확장함수 (연산자 오버로딩이 아니다!)

class TestClass() {
    var a: Int = 0
        private set

    constructor(a: Int) : this() {}
}


//extendFunObj 는 함수객체이고, 타입은  TestClass.(Int) -> Int  (TestClass를 확장하는 함수인데, 인자로 Int를 받고
//Int를 리턴하는 함수이다.) 이다. extendFunObj 에 Int = { a : Int -> a + 3} 을 대입한다.
val extendFunObj: TestClass.(Int) -> Int = { a: Int -> a + 3 }


//A클래스의 확장함수 callback을 인자로 받는다.
//TestClass.(Int) -> Int는 TestClass에 int를 인자로받고 int를 리턴하는 함수를 확장하겠다는 것이다. (아마 이 함수 스코프 내에서만)
fun extendFun(callback: TestClass.(Int) -> Int) {
    val foo = TestClass()
    println(foo.callback(3));
}

/*
확장함수의 람다식 사용 예

class HTML {
    fun body() { ... }
}
​
fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()  // create the receiver object
    html.init()        // pass the receiver object to the lambda
    return html
}
​
//이건 함수호출인 것임.
html {       // lambda with receiver begins here
    body()   // calling a method on the receiver object
}
 */

val pizzaisGreate: String.() -> String = {
    this + "pizza is Greate!"
}

fun extendString(age: Int, name: String): String {
    //this는 확장함수를 호출할 객체이고, it은 인자가 하나일때 쓸 수 있는 예약어.
    val introduceSelf: String.(Int) -> String = { "my name is ${this}, and i'm ${it}" }
    return name.introduceSelf(age)

}

//@ 데이터클래스
data class DataClass(var name: String, var sex: Int, var age: Int);

// object

// companion object


class ObjectTest private constructor() {
    //static과 비슷하지만, 객체 생성시 만들어지기때문에 진짜 static은 아님.
    companion object {
        var age = 10
        fun Create() = ObjectTest()
    }
}

fun printAge() {
    val obj = ObjectTest.Create()
    ObjectTest.age = 3
    println(ObjectTest.age)

}


open class animal(var name: String) {
    //코틀린은 null-safe하다. 모든건 선언과 동시에 초기화해야한다.
    //var a : Int  불가
    //하지만 의존성주입이나 설계상 필요시 선언만 해야할 때도 있다. 그럴때는 lateinit을 붙인다.
    lateinit var a: TestClass
    //lateinit  var a : Int 불가 // 원시타입은 불가능하다.

    open fun bark() {}
}


interface entity {
    fun bark()
}

fun createAnimal() {
    //자바에서 funtional interface를 통해 생성하던 익명클래스, 익명객체를 object를 이용해 구현할 수 있다.
    val A: animal = object : animal("dog") {
        override fun bark() {
            println("멍멍")
        }
    }

    val B: entity = object : entity {
        override fun bark() {
            println("꽥꽥")
        }

    }

}



