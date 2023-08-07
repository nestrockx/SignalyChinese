package com.wegielek.signalychinese.Interfaces;

import com.google.mlkit.vision.digitalink.RecognitionCandidate;

import java.util.List;

public interface CanvasViewListener {
    void onResults(List<RecognitionCandidate> recognitionCandidatesList);
}
