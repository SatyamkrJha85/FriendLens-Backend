import software.amazon.awssdk.services.s3.presigner.S3Presigner;
public class PresignCheck {
    public static void main(String[] args) {
        System.out.println(S3Presigner.builder());
    }
}
