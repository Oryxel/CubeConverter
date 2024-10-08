package org.oryxel.cube.parser.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.oryxel.cube.model.java.ItemModelData;
import org.oryxel.cube.model.java.other.Element;
import org.oryxel.cube.model.java.other.Group;
import org.oryxel.cube.util.Direction;

import java.util.Map;

/*
 * This file is part of CubeConverter - https://github.com/Oryxel/CubeConverter
 * Copyright (C) 2023-2024 Oryxel and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class JavaModelSerializer {

    public static String serializeToString(ItemModelData model) {
        return String.valueOf(serialize(model));
    }

    public static JsonObject serialize(ItemModelData model) {
        JsonObject object = new JsonObject();
        object.addProperty("credit", "Generated by CubeConverter"); // credit
        JsonArray textureSize = new JsonArray(); // texture size
        textureSize.add(model.textureWidth());
        textureSize.add(model.textureHeight());
        object.add("texture_size", textureSize);

        JsonObject textures = new JsonObject(); // textures
        textures.addProperty("0", model.textures()); // #0
        object.add("textures", textures);

        JsonArray elements = new JsonArray();
        JsonArray groups = new JsonArray();

        for (Group group : model.groups()) {
            JsonObject groupObject = new JsonObject();
            groupObject.addProperty("name", group.name());
            groupObject.add("origin", arrayToJsonArray(group.origin()));
            groupObject.addProperty("color", 0);
            groupObject.add("children", mapToJsonArray(group.children()));
            groups.add(groupObject);
        }

        for (Element element : model.elements()) {
            JsonObject jsonElement = new JsonObject();
            jsonElement.addProperty("name", element.name());
            jsonElement.add("from", arrayToJsonArray(element.from()));
            jsonElement.add("to", arrayToJsonArray(element.to()));
            jsonElement.add("rotation", buildRotationObject(element));
            jsonElement.add("faces", buildFacesObject(element));
            elements.add(jsonElement);
        }

        object.add("elements", elements);
        object.add("groups", groups);

        return object;
    }

    private static JsonObject buildFacesObject(Element element) {
        JsonObject faces = new JsonObject();

        for (Map.Entry<Direction, double[]> direction : element.uvMap().entrySet()) {
            JsonObject faceDirection = new JsonObject();
            JsonArray array = arrayToJsonArray(direction.getValue());
            if (array == null)
                continue;

            faceDirection.add("uv", array);
            faceDirection.addProperty("texture", "#0");

            faces.add(direction.getKey().name().toLowerCase(), faceDirection);
        }

        if (faces.isEmpty()) {
            JsonObject face = new JsonObject();
            face.add("uv", arrayToJsonArray(new double[4]));
            face.addProperty("texture", "#0");
            faces.add("down", face);
        }

        return faces;
    }

    private static JsonObject buildRotationObject(Element element) {
        JsonObject rotation = new JsonObject();
        rotation.addProperty("angle", element.angle());
        rotation.addProperty("axis", element.axis());
        rotation.add("origin", arrayToJsonArray(element.origin()));

        return rotation;
    }

    private static JsonArray arrayToJsonArray(double[] array) {
        if (array == null)
            return null;

        JsonArray array1 = new JsonArray();
        for (double d : array) {
            array1.add(d);
        }

        return array1;
    }

    private static JsonArray mapToJsonArray(Map<Integer, Element> map) {
        JsonArray array1 = new JsonArray();
        for (Map.Entry<Integer, Element> d : map.entrySet()) {
            array1.add(d.getKey());
        }

        return array1;
    }

}
