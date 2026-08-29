package com.azarasi.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class AudioManager {
    private static AudioPlayerManager playerManager;
    private static AudioPlayer audioPlayer;

    public static void init() {
        playerManager = new DefaultAudioPlayerManager();
        // YouTube, SoundCloud等のソースマネージャーを登録
        AudioSourceManagers.registerRemoteSources(playerManager);
        audioPlayer = playerManager.createPlayer();
    }

    public static void playUrl(String url, MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            playerManager.loadItem(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    server.getPlayerManager().broadcast(
                        Text.literal("▶ 再生開始: " + track.getInfo().title), false
                    );
                    audioPlayer.playTrack(track);
                    // ここでデコードされたオーディオフレームをマイクラの音声チャンネルに一斉配信する
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    server.getPlayerManager().broadcast(
                        Text.literal("▶ プレイリストから再生: " + firstTrack.getInfo().title), false
                    );
                    audioPlayer.playTrack(firstTrack);
                }

                @Override
                public void noMatches() {
                    server.getPlayerManager().broadcast(
                        Text.literal("❌ 指定されたURLから曲が見つからなかったよ。"), false
                    );
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    server.getPlayerManager().broadcast(
                        Text.literal("❌ 再生エラー: " + exception.getMessage()), false
                    );
                }
            });
        });
    }

    public static void stop() {
        if (audioPlayer != null) {
            audioPlayer.stopTrack();
        }
    }
}
