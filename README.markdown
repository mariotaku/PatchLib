#PatchLib

---



Goodbye, Java reflection!

Let's pretend you are using a good library that fits almost all your need, except one thing - some useful fields or classes are private, so you can't access in normal ways.

It's extremely painful to use relections.

````
AClass aInstance = someMethod();
Class<?> cls = Class.forName("com.example.ADefaultClass");
cls.setAccessible(true);
Field field = cls.getDeclaredField("aPrivateField");
field.setAccessible(true);
Object fieldObj = field.get(aInstance);
Class<?> fieldCls = Class.forName("com.example.APrivateFieldType");
Method classMethod = fieldCls.getMethod("privateMethod", Object.class);
classMethod.invoke(fieldObj, "Parameter");
// ... Exceptions are even not included in this example!

````
Hey! since classes in those libraries will be shipped with our binary, why can't we modify it?

With PatchLib, all you need to do is write a rule file in .yml format like this:


````
- com.example.ADefaultClass:
  modifiers: +public # Make public
  fields:
    - aPrivateField:
      modifiers: +public
- com.example.APrivateFieldType:
  methods:
    - privateMethod: # Will add signatures in future versions
      modifiers: +public

````

... and use your desired classes like this:


````
AClass aInstance = someMethod();
aInstance.aPrivateField.privateMethod("Parameter");

````