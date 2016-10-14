/*
 * Copyright (c) 2016. Roberto  Prato <https://github.com/robertoprato>
 *
 *  *
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package io.square1.richtextlib.ui.video;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.square1.richtextlib.R;

/**
 * Created by roberto on 14/10/2016.
 */

class VideoControls {

    private FrameLayout mControlsContainer;
    private View mPlayButton;
    private View mPauseButton;
    private RichVideoView mVideoView;

    public VideoControls(Context context,
                         RichVideoView display, FrameLayout container){

        mVideoView = display;

        mControlsContainer = (FrameLayout) LayoutInflater.from(context)
                .inflate(R.layout.internal_richtext_video_controller,
                        container,
                        false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mControlsContainer.setLayoutParams(params);
        container.addView(mControlsContainer, params);

        mControlsContainer.setVisibility(View.INVISIBLE);
        mPlayButton = mControlsContainer.findViewById(R.id.play);
        mPauseButton = mControlsContainer.findViewById(R.id.pause);

        View.OnClickListener controlsClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(v.getId() == mPlayButton.getId()){
                    mVideoView.start();
                }
                else if(v.getId() == mPauseButton.getId()){
                    if(mVideoView.isPlaying() == true ){
                        mVideoView.pause();
                    }
                }else if(v.getId() == R.id.full_screen){
                    mVideoView.openFullScreen();
                }

                updateControls();
            }
        };


        mPlayButton.setOnClickListener(controlsClickListener);
        mPauseButton.setOnClickListener(controlsClickListener);

        mControlsContainer.findViewById(R.id.full_screen)
                .setOnClickListener(controlsClickListener);

    }
    public VideoControls(Context context, RichVideoView parent){
        this(context, parent, parent);
    }


    private Runnable mHideControls = new Runnable() {
        @Override
        public void run() {
            mControlsContainer.setVisibility(View.INVISIBLE);
            updateControls();
        }
    };


    public void showControls(){

        if(mControlsContainer.getVisibility() != View.VISIBLE){
            mControlsContainer.setVisibility(View.VISIBLE);

        }
        updateControls();
        mControlsContainer.removeCallbacks(mHideControls);
        mControlsContainer.postDelayed(mHideControls, 3000);

    }


    void updateControls(){
        mPauseButton.setVisibility(mVideoView.isPlaying() ? View.VISIBLE : View.GONE);
        mPlayButton.setVisibility(!mVideoView.isPlaying() ? View.VISIBLE : View.GONE);
    }

}
