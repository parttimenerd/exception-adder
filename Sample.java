/** A sample class with ten methods that are called randomly */
public class Sample {
    public void method1() {
        System.out.println("Method 1 executed");
    }

    public void method2() {
        System.out.println("Method 2 executed");
    }

    public void method3() {
        System.out.println("Method 3 executed");
    }

    public void method4() {
        System.out.println("Method 4 executed");
    }

    public void method5() {
        System.out.println("Method 5 executed");
    }

    public void method6() {
        System.out.println("Method 6 executed");
    }

    public void method7() {
        System.out.println("Method 7 executed");
    }

    public void method8() {
        System.out.println("Method 8 executed");
    }

    public void method9() {
        System.out.println("Method 9 executed");
    }

    public void method10() {
        System.out.println("Method 10 executed");
    }

    public static void main(String[] args) {
        Sample sample = new Sample();
        for (int i = 0; i < 10; i++) {
            int methodNumber = (int) (Math.random() * 10) + 1;
            switch (methodNumber) {
                case 1 -> sample.method1();
                case 2 -> sample.method2();
                case 3 -> sample.method3();
                case 4 -> sample.method4();
                case 5 -> sample.method5();
                case 6 -> sample.method6();
                case 7 -> sample.method7();
                case 8 -> sample.method8();
                case 9 -> sample.method9();
                case 10 -> sample.method10();
            }
        }
    }
}