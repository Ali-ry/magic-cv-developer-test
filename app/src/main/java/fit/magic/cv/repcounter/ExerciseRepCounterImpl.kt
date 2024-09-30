// Copyright (c) 2024 Magic Tech Ltd

package fit.magic.cv.repcounter
import kotlin.math.*

import fit.magic.cv.PoseLandmarkerHelper

class ExerciseRepCounterImpl : ExerciseRepCounter() {

    override fun setResults(resultBundle: PoseLandmarkerHelper.ResultBundle) {
        // process pose data in resultBundle
        //
        // use functions in base class incrementRepCount(), sendProgressUpdate(),
        // and sendFeedbackMessage() to update the UI

        //We assume we already have got the pose landmarks from a different class and function.

        // we store the landmarks in an array type data structure.
        val landmarks = resultBundle.poseLandmarks


        //in an Alternating Lunge exercise, the key body movements involve bending at the hips, knees, and ankles
        // to perform the forward lunge, as well as maintaining balance and proper posture through
        // the shoulders and hips.
        // landmark indices for alternating lunge - comes from documentaiton:
        val leftHipIndex = 23
        val leftKneeIndex = 25
        val leftAnkleIndex = 27
        val rightHipIndex = 24
        val rightKneeIndex = 26
        val rightAnkleIndex = 28
        //val leftShoulderIndex = 11
        //val rightShoulderIndex = 12

        // Ensure the required body locations are available and visible
        if(landmarks.size > rightAnkleIndex > 0.5 &&
            landmarks[leftHipIndex].visibility > 0.5 &&
            landmarks[leftKneeIndex].visibility > 0.5 &&
            landmarks[leftAnkleIndex].visibility > 0.5 &&
            landmarks[rightHipIndex].visibility > 0.5 &&
            landmarks[rightKneeIndex].visibility > 0.5 &&
            landmarks[rightAnkleIndex].visibility > 0.5)
        {
            // get left leg landmarks
            val leftHip = landmarks[leftHipIndex]
            val leftKnee = landmarks[leftKneeIndex]
            val leftAnkle = landmarks[leftAnkleIndex]

            // get right leg landmarks
            val rightHip = landmarks[rightHipIndex]
            val rightKnee = landmarks[rightKneeIndex]
            val rightAnkle = landmarks[rightAnkleIndex]

            // Calculate angles for both legs to check for progress and for rep completion
            val leftLegAngle = calculateAngle3D(leftHip, leftKnee, leftAnkle)
            val rightLegAngle = calculateAngle3D(rightHip, rightKnee, rightAnkle)

            //  thresholds for lunges - we assume full extension threshold is 160 degrees vs bent is ~90 degrees
            val startPositionAngle = 160f
            val endPositionAngle = 90f

            // Calculating progress based on left and right leg angles
            val leftProgress = calculateProgress(leftLegAngle, startPositionAngle, endPositionAngle)
            val rightProgress = calculateProgress(rightLegAngle, startPositionAngle, endPositionAngle)

            // check to see if a repetition is completed for the left leg
            if (detectRepetitionCompletion(leftLegAngle, lastLeftLegAngle, startPositionAngle, endPositionAngle)) {
                incrementRepCount() // Call the base class method to update repetition count
            }

            //  check to see if a repetition is completed for the right leg
            if (detectRepetitionCompletion(rightLegAngle, lastRightLegAngle, startPositionAngle, endPositionAngle)) {
                incrementRepCount() // Call the base class method to update repetition count
            }


            // updating the UI with progress
            sendProgressUpdate((leftProgress + rightProgress) / 2) // Average progress

            // Storing the last angles for next/future comparison
            lastLeftLegAngle = leftLegAngle
            lastRightLegAngle = rightLegAngle
        }


    }



    // Function to calculate the 3D angle between three landmarks (hip, knee, ankle)
    private fun calculateAngle3D(a: PoseLandmarkerHelper.Landmark, b: PoseLandmarkerHelper.Landmark, c: PoseLandmarkerHelper.Landmark): Float {


        //calculating the vectors between the 3 landmarks
        //calculating the length of the vectors
        //calculaging the angle

        val abx = a.x - b.x
        val aby = a.y - b.y
        val abz = a.z - b.z

        val bcx = c.x - b.x
        val bcy = c.y - b.y
        val bcz = c.z - b.z

        //The rest of the calculating to be worked out
        //TO BE COMPLETED
    }
    // a function to calculate progress (0 to 1) based on current angle
    private fun calculateProgress(currentAngle: Float, startAngle: Float, endAngle: Float): Float {
        return 1 - ((currentAngle - endAngle) / (startAngle - endAngle)).coerceIn(0f, 1f)
    }       //Expample  125           90           160          90

    private var isInBendingPhase = false

    private fun detectRepetitionCompletion(currentAngle: Float, lastAngle: Float, startAngle: Float, endAngle: Float): Boolean {
        // Detect the bending phase (moving towards the fully bent position)
        if (currentAngle <= endAngle) {
            isInBendingPhase = true
        }

        // Detect the extension phase and check if bending occurred first
        if (isInBendingPhase && lastAngle < currentAngle && currentAngle >= startAngle) {
            // Reset phase after counting a rep
            isInBendingPhase = false
            return true
        }

        return false
    }
    // Variables we use to store the last angles for comparison
    private var lastLeftLegAngle: Float = 160f // we assume the left leg is initially extended
    private var lastRightLegAngle: Float = 160f // we assume the right leg is initially extended
}


