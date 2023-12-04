public class Main {

    public static void main(String[] args) {
        TestProcessor.runTests(TestClass.class);
    }

    static class TestClass {

        @BeforeEach
        void beforeEach() {
            System.out.println("beforeEach запущен");
        }

        @AfterEach
        void afterEach() {
            System.out.println("afterEach запущен");
        }

        @Test()
        void second(){
            System.out.println("second запущен");
        }
        @Skip
        @Test(order = -2)
        void first(){
            System.out.println("first запущен");
        }

        @Test(order = 5)
        void third(){
            System.out.println("third запущен");
        }
    }

}
