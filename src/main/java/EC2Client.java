import com.amazonaws.regions.Regions;

import java.io.*;
import java.util.ArrayList;

public class EC2Client {

    public static void main(String[] args)
    {
        try
        {
            String filename = args[0];
            String line;
            Regions region = Regions.US_EAST_1;
            S3Handler s3Input = new S3Handler(region, "ccfoebucket");
            S3Handler s3Output = new S3Handler(region, "ccoutputbucket");
            File f = new File("/home/ubuntu/darknet/results_parsed");

            if(!f.exists())
                f.mkdir();

            File saveDir = new File("/home/ubuntu/darknet/videos");

            if(!saveDir.exists())
                saveDir.mkdir();

            // Download video from S3
            s3Input.Download(filename, "/home/ubuntu/darknet/videos/");

            // Run darknet with video as input to darknet
            ProcessBuilder p = new ProcessBuilder()
                    .command("Xvfb", ":1", "&", "export","DISPLAY=:1",";","/home/ubuntu/darknet/darknet", "detector", "demo", "/home/ubuntu/darknet/cfg/coco.data", "/home/ubuntu/darknet/cfg/yolov3-tiny.cfg", "/home/ubuntu/darknet/yolov3-tiny.weights",  "/home/ubuntu/darknet/videos/" + filename)
                    .redirectOutput(new File("/home/ubuntu/darknet/results/" + filename));

            Process process = p.start();
            process.waitFor();

            // Read and parse results
            BufferedReader reader = new BufferedReader(new FileReader("/home/ubuntu/darknet/results/" + filename));
            String resultString = "";
            int flag = 0;
            ArrayList<String> objectList = new ArrayList<>();
            while((line = reader.readLine()) !=null)
            {
                //System.out.println("reading file");
                if(line.equals("Objects:"))
                {
                    //System.out.println("flag set");
                    flag =1;

                }

                else if (line.contains("FPS:"))
                {
                    //System.out.println("flag unset");
                    flag =0;
                }
                else if(flag == 1 && !line.isEmpty() && !line.contains("[1;1H"))
                {
                    //System.out.println("got object");
                    System.out.println(line);
                    String object = line.split(":")[0];
                    if(!objectList.contains(object))
                    {
                        objectList.add(object);
                        resultString += ',' + object;
                    }
                }
            }


            System.out.println("result" + resultString);
            if(resultString.isEmpty())
                resultString = "No Object Detected";
            else
                resultString = resultString.substring(1);

            System.out.println("result" + resultString);
            BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ubuntu/darknet/results_parsed/" + filename));
            writer.write(resultString);
            writer.close();

            // Upload results to S3
            s3Output.Upload("/home/ubuntu/darknet/results_parsed/" + filename);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
