package com.azarasi.music;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MusicMod implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // Lavaplayerオーディオマネージャーの初期化
        AudioManager.init();

        // /play <URL> コマンドの登録
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("play")
                .then(CommandManager.argument("url", StringArgumentType.string())
                    .executes(context -> {
                        String url = StringArgumentType.getString(context, "url");
                        var server = context.getSource().getServer();
                        ServerPlayerEntity player = context.getSource().getPlayer();

                        String senderName = player != null ? player.getName().getString() : "サーバー";

                        // 全員に再生リクエストのチャット通知
                        server.getPlayerManager().broadcast(
                            Text.literal("♪ " + senderName + " が曲を再生リクエストしたよ！"), false
                        );

                        // 非同期で音声ストリームを取得して再生
                        AudioManager.playUrl(url, server);

                        return 1;
                    })
                )
            );
        });
    }
}
