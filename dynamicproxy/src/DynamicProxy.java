import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by RichardYuan on 2017/5/29 0029.
 */
public class DynamicProxy {


    public static void main(String[] args) {
        Movable d = new Dog();
        Movable m = (Movable) Proxy.newProxyInstance(Dog.class.getClassLoader(), new Class[] {Movable.class}, new Delegation(d));
        System.out.println(m.move());
    }

    private static class Delegation implements InvocationHandler {

        Object source;

        public Delegation(Object source) {
            this.source = source;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("before");
            try {
                return method.invoke(source, args);
            } finally {
                System.out.println("after");

            }
        }
    }


    private static class Dog implements Movable {
        @Override
        public String move() {
            System.out.println("doge move");
            return "abc";
        }
    }

    private interface Movable {
        String move();
    }


}

