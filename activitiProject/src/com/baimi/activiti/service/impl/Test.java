package com.baimi.activiti.service.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

class  ClassA <T>{       
    private T obj;       
    public void setObject(T obj) {      this.obj = obj;  }     
    public T getObject() {    return obj;   }       
}    
public class Test<T> extends ClassA<T>{     
    private List<String> list;     
  
    public void testA() throws NoSuchFieldException, SecurityException{  
        Type t = Test.class.getDeclaredField("list").getGenericType();  
        if (ParameterizedType.class.isAssignableFrom(t.getClass())) {              
            for (Type t1:((ParameterizedType)t).getActualTypeArguments()) {          
                System.out.print(t1 + ",");          
            }          
            System.out.println();          
        }   
   }  
   public static void main(String args[]) throws Exception{       
            System.out.println("======getSuperclass======:");      
            System.out.println(Test.class.getSuperclass().getName());     
            System.out.println("======getGenericSuperclass======:");   
            Type t = Test.class.getGenericSuperclass();      
            System.out.println(t);       
            if (ParameterizedType.class.isAssignableFrom(t.getClass())) {    
                     System.out.print("----------->getActualTypeArguments:");       
                     for (Type t1:((ParameterizedType)t).getActualTypeArguments()) {       
                                    System.out.print(t1 + ",");       
                      }       
                     System.out.println();       
            }    
   }     
  
  }    
