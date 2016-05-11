/*
 *  Author(s): Kosuke Miyoshi, Narrative Nights
 */
package com.yurisuzuki.ar;

import com.yurisuzuki.CameraActivity;
import org.artoolkit.ar.base.ARToolKit;

import javax.microedition.khronos.opengles.GL10;

public class GuitarCodeMarker extends Marker {
	/// trackingが外れた後に、holdを継続する時間
	private static final long HOLDING_DURATION_MILLIS = 30 * 1000;

	private long holdStartTime = -1L;

	void checkHold(long now, CameraActivity activity) {
		if (isTracked()) {
			// マーカーを認識していたら、lastTrackedTimeを更新
			lastTrackedTime = now;
			holdStartTime = -1L;
			activity.suppressCurrentSound(soundId);
		} else {
			if (lastTrackedTime > 0 && holdStartTime < 0) {
				// hold開始
				lastTrackedTime = -1;
				holdStartTime = now;
				activity.setCurrentSound(soundId);
			} else if (lastTrackedTime == -1L && holdStartTime >= 0) {
				// hold中
				if (now - holdStartTime > HOLDING_DURATION_MILLIS) {
					// hold開始してから一定時間経ったのでholdを終了する
					activity.stopCurrentSound(soundId);
					lastTrackedTime = -1L;
					holdStartTime = -1L;
				}
			}
		}
	}

	void draw(GL10 gl, long now, boolean front) {
		if (isTracked()) {
			float markerMatrix[] = ARToolKit.getInstance().queryMarkerTransformation(markerId);

			if( front ) {
				// 反転させる
				markerMatrix[1] = -markerMatrix[1];
				markerMatrix[5] = -markerMatrix[5];
				markerMatrix[9] = -markerMatrix[9];
				markerMatrix[13] = -markerMatrix[13];
			}

			if (markerMatrix != null) {
				cacheMarkerMatrix(markerMatrix);
			}

			if (lastTrackedTime > 0) {
				if (markerMatrix != null) {
					gl.glLoadMatrixf(markerMatrix, 0);
					markerPlane.draw(gl);
				}
			}
		}

		if (holdStartTime >= 0 && markerMatrixCached) {
			gl.glLoadMatrixf(cachedMarkerMatrix, 0);
			actionPlane.draw(gl);
		}
	}
}