package it.ethereallabs.etherealperms.ui

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec

class EditorEventData {
    var action: String = ""
    var targetName: String = ""
    var targetType: String = ""

    constructor()

    companion object {
        val CODEC: BuilderCodec<EditorEventData> = BuilderCodec.builder(EditorEventData::class.java, ::EditorEventData)
            .append(KeyedCodec("Action", Codec.STRING), { e, v -> e.action = v }, { e -> e.action })
            .add()
            .append(KeyedCodec("TargetName", Codec.STRING), { e, v -> e.targetName = v }, { e -> e.targetName })
            .add()
            .append(KeyedCodec("TargetType", Codec.STRING), { e, v -> e.targetType = v }, { e -> e.targetType })
            .add()
            .build()
    }
}