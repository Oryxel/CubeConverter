package org.oryxel.cube.parser.bedrock;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.GsonUtil;
import org.oryxel.cube.model.bedrock.BedrockRenderController;
import org.oryxel.cube.model.bedrock.controller.ControllerCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

// THIS IS NOT READY, STILL NEED TO COMPLETE Evaluation
public class BedrockControllerSerializer {

    public static List<BedrockRenderController> deserialize(String json) {
        return deserialize(GsonUtil.getGson().fromJson(json.trim(), JsonObject.class));
    }

    public static List<BedrockRenderController> deserialize(JsonObject json) {
        if (!json.has("render_controllers")) {
            return new ArrayList<>();
        }

        final List<BedrockRenderController> list = new ArrayList<>();

        JsonObject controllers = json.getAsJsonObject("render_controllers");
        for (String controllerName : controllers.keySet()) {
            if (!controllers.get(controllerName).isJsonObject() || !controllerName.startsWith("controller.render"))
                continue;

            final Map<String, List<String>> geometries = new HashMap<>(),
                    textures = new HashMap<>();

            JsonObject object = controllers.getAsJsonObject(controllerName);
            JsonObject arrays = object.getAsJsonObject("arrays");
            if (arrays == null)
                return null;

            if (arrays.has("textures") && arrays.get("textures").isJsonArray()) {
                JsonObject arraysTextures = arrays.getAsJsonObject("textures");
                for (String name : arraysTextures.keySet()) {
                    if (!arraysTextures.get(name).isJsonArray() || !name.startsWith("Array."))
                        continue;

                    JsonArray texturesObject = arraysTextures.getAsJsonArray(name);
                    if (texturesObject == null)
                        continue;

                    final List<String> texturesList = new ArrayList<>();

                    for (JsonElement texture : texturesObject) {
                        texturesList.add(texture.getAsString().replace("Texture.", ""));
                    }

                    textures.put(name.replace("Array.", ""), texturesList);
                }
            }

            if (arrays.has("geometries") && arrays.get("geometries").isJsonArray()) {
                JsonObject geoTextures = arrays.getAsJsonObject("geometries");
                for (String name : geoTextures.keySet()) {
                    if (!geoTextures.get(name).isJsonArray() || !name.startsWith("Array."))
                        continue;

                    JsonArray geoArray = geoTextures.getAsJsonArray(name);
                    if (geoArray == null)
                        continue;

                    final List<String> geometriesList = new ArrayList<>();

                    for (JsonElement texture : geoArray) {
                        geometriesList.add(texture.getAsString().replace("Geometry.", ""));
                    }

                    geometries.put(name.replace("Array.", ""), geometriesList);
                }
            }

            Map<String, String> textureIndex = new HashMap<>(), geometryIndex = new HashMap<>();
            if (object.has("textures"))
                textureIndex = getListFromJson(object.getAsJsonArray("textures"));

            if (object.has("geometry")) {
                JsonElement element = object.get("geometry");
                if (element.isJsonPrimitive())
                    geometryIndex.put("", object.getAsJsonPrimitive("geometry").getAsString().replace("Geometry.", ""));
                else geometryIndex = getListFromJson(element.getAsJsonArray());
            }

            System.out.println(controllerName);
            list.add(new BedrockRenderController(controllerName, textureIndex, geometryIndex, textures, geometries));
        }

        return list;
    }

    private static Map<String, ControllerCondition> getController(JsonArray controller) {
        if (controller.isEmpty())
            return new HashMap<>();

        final Map<String, ControllerCondition> map = new HashMap<>();

        for (JsonElement element : controller) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                for (String name : object.keySet()) {
                    JsonElement element1 = object.get(name);
                    if (!element1.isJsonPrimitive())
                        continue;

                    int testId = -1;
                    try {
                        testId = Integer.parseInt(String.valueOf(element1));
                    } catch (Exception e) {}

                    if (testId != -1)
                        map.put(name + ".part" + testId, new ControllerCondition("", "", ControllerCondition.ConditionType.NONE));
                    else {
                        String[] split = element1.toString().replace("\"", "").split(" ");
                        if (split.length != 3)
                            continue;
                        String type = split[0].replace("q.", "").toUpperCase();
                        String compare = split[1];
                        ControllerCondition.ConditionType conditionType = compare.equals("==") ? ControllerCondition.ConditionType.EQUALS :
                                compare.equals(">") ? ControllerCondition.ConditionType.LARGER : compare.equals("<") ? ControllerCondition.ConditionType.SMALLER :
                                        ControllerCondition.ConditionType.NONE;

                        map.put(name, new ControllerCondition(type, compare, conditionType));
                    }
                }
            } else {
                map.put(element.getAsString(), new ControllerCondition("", "", ControllerCondition.ConditionType.NONE));
                break; // there is no condition, we should be safe to break
            }
        }

        return map;
    }

    private static Map<String, String> getListFromJson(JsonArray array) {
        Map<String, String> map = new HashMap<>();

        for (JsonElement element : array) {
            String s = element.getAsString().replace("Array.", "");
            String[] split = s.split("\\[");
            if (split.length != 2 || split[0].split(" ").length > 1)
                continue;

            System.out.println(array);
            map.put(split[0].replace("Array.", ""), split[1].replace("]", ""));
        }

        return map;
    }

}
