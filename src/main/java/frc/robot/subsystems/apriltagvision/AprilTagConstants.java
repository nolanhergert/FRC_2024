package frc.robot.subsystems.apriltagvision;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;

public final class AprilTagConstants {
  public static enum CameraResolution {
    HIGH_RES,
    NORMAL
  }

  public static final Transform3d frontCamToRobot =
      new Transform3d(
          new Translation3d(
              Units.inchesToMeters(11.32), Units.inchesToMeters(7.08), Units.inchesToMeters(7.8)),
          new Rotation3d(0.0, Units.degreesToRadians(-30), 0.0));
  public static final Transform3d leftCamToRobot =
      new Transform3d(
          new Translation3d(
              Units.inchesToMeters(-12.01),
              Units.inchesToMeters(11.65),
              Units.inchesToMeters(10.58)),
          new Rotation3d(0.0, Units.degreesToRadians(-23.5), Units.degreesToRadians(147)));
  public static final Transform3d rightCamToRobot =
      new Transform3d(
          new Translation3d(
              Units.inchesToMeters(-12.01),
              Units.inchesToMeters(-11.65),
              Units.inchesToMeters(10.58)),
          new Rotation3d(0.0, Units.degreesToRadians(-23.5), Units.degreesToRadians(-147)));
  public static final Matrix<N3, N1> highResSingleTagStdDev =
      VecBuilder.fill(0.25, 0.25, Double.MAX_VALUE);
  public static final Matrix<N3, N1> normalSingleTagStdDev =
      VecBuilder.fill(0.5, 0.5, Double.MAX_VALUE);
  public static final Matrix<N3, N1> highResMultiTagStdDev = VecBuilder.fill(0.1, 0.1, 2);
  public static final Matrix<N3, N1> normalMultiTagStdDev = VecBuilder.fill(0.2, 0.2, 4);
}
