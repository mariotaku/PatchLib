#PatchLib

---

## In very early stage of development!

Goodbye, Java reflection!

Let's pretend you are using a good library that fits almost all your need, except one thing - some useful fields or classes are private, so you can't access in normal ways.

It's extremely painful to use reflections.

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
rules:
  - com/example/ADefaultClass:
    modifiers: +public # Make public
    fields:
      - aPrivateField:
        modifiers: +public # Make public
  - com/example/APrivateFieldType:
    modifiers: +public # Make public
    methods:
      - privateMethod:
        modifiers: +public # Make public
        
````

... and use your desired classes like this:


````
AClass aInstance = someMethod();
aInstance.aPrivateField.privateMethod("Parameter");

````

That's it!

---

There're more advanced usages:

````
rules: # Let you throw custom exceptions in Retrofit
  /retrofit/Converter: # Will match all subclasses if name beginning with a slash
    methods:
      convert:
        exceptions: +com/example/CustomException
  /retrofit/RequestBuilderAction:
    methods:
      perform:
        exceptions: +com/example/CustomException
  /retrofit/RequestFactory:
    methods:
      create:
        exceptions: +com/example/CustomException
  retrofit/OkHttpCall:
    methods:
      parseResponse:
        exceptions: +com/example/CustomException
      createRawCall:
        exceptions: +com/example/CustomException
  /retrofit/Call:
    methods:
      execute:
        exceptions: +com/example/CustomException
  /retrofit/CallAdapter:
    methods:
      adapt:
        exceptions: +com/example/CustomException
  retrofit/MethodHandler:
    methods:
      invoke:
        exceptions: +com/example/CustomException

````

The library not exactly matches your need? Patch it! No more reflections, no more waiting for pull request approval, no more meed to fork repos and merge code changes, just write rules and enjoy! 