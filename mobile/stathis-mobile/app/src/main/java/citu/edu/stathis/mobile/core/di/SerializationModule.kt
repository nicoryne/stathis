package citu.edu.stathis.mobile.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Singleton
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.LessonContent
import citu.edu.stathis.mobile.features.tasks.data.model.LessonPage
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonParseException

@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .registerTypeAdapter(LessonTemplate::class.java, LessonTemplateAdapter())
            .create()
    }
}

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(
        src: LocalDateTime,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDateTime {
        return LocalDateTime.parse(json.asString, formatter)
    }
}

class LessonTemplateAdapter : JsonDeserializer<LessonTemplate> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LessonTemplate {
        if (!json.isJsonObject) throw JsonParseException("LessonTemplate should be an object")
        val obj = json.asJsonObject

        val physicalId = obj.get("physicalId")?.asString ?: ""
        val title = obj.get("title")?.asString ?: ""
        val description = obj.get("description")?.asString ?: ""

        val contentObj: JsonObject = obj.getAsJsonObject("content") ?: JsonObject()
        val pagesArray: JsonArray = contentObj.getAsJsonArray("pages") ?: JsonArray()

        val pages: MutableList<LessonPage> = mutableListOf()
        for (pageEl in pagesArray) {
            if (!pageEl.isJsonObject) continue
            val pObj = pageEl.asJsonObject
            val id = pObj.get("id")?.asString ?: ""
            val pageNumber = pObj.get("pageNumber")?.asInt ?: 0
            val subtitle = pObj.get("subtitle")?.asString ?: ""

            val paragraphEl = pObj.get("paragraph")
            val paragraph: List<String> = when {
                paragraphEl == null || paragraphEl.isJsonNull -> emptyList()
                paragraphEl.isJsonArray -> paragraphEl.asJsonArray.mapNotNull { it.asString }
                paragraphEl.isJsonPrimitive -> listOf(paragraphEl.asString)
                else -> emptyList()
            }

            pages.add(
                LessonPage(
                    id = id,
                    pageNumber = pageNumber,
                    subtitle = subtitle,
                    paragraph = paragraph
                )
            )
        }

        return LessonTemplate(
            physicalId = physicalId,
            title = title,
            description = description,
            content = LessonContent(pages = pages)
        )
    }
}