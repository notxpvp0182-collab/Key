package com.keystrokeshud.input;

import net.minecraft.client.MinecraftClient;
import java.util.ArrayDeque;
import java.util.Deque;

public class InputTracker {

    private static final long WINDOW_MS = 1000L;

    private final Deque<Long> leftClicks  = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    private int cachedLCPS = 0;
    private int cachedRCPS = 0;

    private boolean prevLmb, prevRmb;

    public float animW, animA, animS, animD, animLMB, animRMB;
    private static final float ANIM_SPEED = 0.25f;

    public void onRender(MinecraftClient client) {
        boolean w   = client.options.forwardKey.isPressed();
        boolean a   = client.options.leftKey.isPressed();
        boolean s   = client.options.backKey.isPressed();
        boolean d   = client.options.rightKey.isPressed();
        boolean lmb = client.options.attackKey.isPressed();
        boolean rmb = client.options.useKey.isPressed();

        long now = System.currentTimeMillis();
        if (lmb && !prevLmb) leftClicks.addLast(now);
        if (rmb && !prevRmb) rightClicks.addLast(now);
        prevLmb = lmb;
        prevRmb = rmb;

        animW   = lerp(animW,   w   ? 1f : 0f);
        animA   = lerp(animA,   a   ? 1f : 0f);
        animS   = lerp(animS,   s   ? 1f : 0f);
        animD   = lerp(animD,   d   ? 1f : 0f);
        animLMB = lerp(animLMB, lmb ? 1f : 0f);
        animRMB = lerp(animRMB, rmb ? 1f : 0f);
    }

    public void onTick() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        while (!leftClicks.isEmpty()  && leftClicks.peekFirst()  < cutoff) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && rightClicks.peekFirst() < cutoff) rightClicks.pollFirst();
        cachedLCPS = leftClicks.size();
        cachedRCPS = rightClicks.size();
    }

    public int getLeftCPS()  { return cachedLCPS; }
    public int getRightCPS() { return cachedRCPS; }

    private static float lerp(float cur, float target) {
        float d = target - cur;
        return Math.abs(d) < 0.005f ? target : cur + d * ANIM_SPEED;
    }
        }
