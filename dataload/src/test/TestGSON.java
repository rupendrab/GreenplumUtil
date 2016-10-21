package test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TestGSON
{
    public static void main(String[] args)
    {
        String json = "{\"name\": \"Rupendra Bandyopadhyay\", \"phone\": \"3031112222\"}";
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(json, JsonObject.class);
        System.out.println(obj.get("name"));
        System.out.println(obj.get("phone"));
        JsonObject obj2 = new JsonObject();
        obj2.addProperty("name", "Nabanita Raul");
        obj2.addProperty("phone", "7203334555");
        System.out.println(obj2.toString());
        JsonArray obj3 = new JsonArray();
        obj3.add(obj);
        obj3.add(obj2);
        System.out.println(obj3);
    }

}
