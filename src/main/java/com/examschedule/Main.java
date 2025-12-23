import com.examschedule.models.*;
import com.examschedule.utils.DataLoader;
import com.examschedule.utils.ScheduleFitness;
import com.examschedule.utils.SchedulePrinter;
import com.examschedule.algorithms.AntColonyOptimization;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("========== EXAM SCHEDULING WITH ACO ==========\n");
        
        // Bước 1: Xác định bộ test (Chỉ cần đổi số này để chạy bộ khác)
        int testNumber =10;
        String inputFileName = "test_inputs/input_test" + testNumber + ".json";
        String outputFileName = "test_outputs/output_test" + testNumber + ".json";

        System.out.println("========== RUNNING TEST CASE: " + testNumber + " ==========");
        
        // Nạp dữ liệu
        ScheduleData data = DataLoader.loadFromJSON(inputFileName);
        if (data == null) {
            System.err.println("[Main] Failed to load data from: " + inputFileName);
            return;
        }
        System.out.println("[Main] Data loaded successfully.\n");

        // Bước 2: Khởi tạo ACO
        int numAnts = 500;
        int maxIterations = 200;
        AntColonyOptimization aco = new AntColonyOptimization(data, numAnts, maxIterations);
        System.out.println("[Main] ACO configured: " + numAnts + " ants, " + maxIterations + " iterations\n");

        // Bước 3: Tối ưu hoá
        ScheduleOutput result = aco.solve();
        System.out.println("[Main] Optimization complete\n");

        // Bước 4: Phân tích và Hiển thị kết quả
        ScheduleFitness fitnessTool = new ScheduleFitness(data);
        fitnessTool.printScheduleAnalysis(result.getSchedule(), result.getFitness());
        
        SchedulePrinter.printScheduleTable(result.getSchedule(), data);

        // Bước 5: Ghi ra file JSON (Truyền thêm tham số outputFileName)
        writeOutputJSON(result.getSchedule(), result.getFitness(), outputFileName);
        
        System.out.println("\n[Main] Done!");
    }

    /**
     * Hàm ghi kết quả ra file JSON
     * @param schedule Danh sách phân bổ
     * @param fitness Điểm tối ưu
     * @param fileName Đường dẫn file đầu ra
     */
    private static void writeOutputJSON(List<Assignment> schedule, double fitness, String fileName) {
        try {
            // 1. Đảm bảo thư mục đích tồn tại
            Path path = Paths.get(fileName);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // 2. Tạo cấu trúc JSON
            JSONObject output = new JSONObject();
            JSONArray scheduleArray = new JSONArray();
            
            for (Assignment assignment : schedule) {
                JSONObject assignmentObj = new JSONObject();
                assignmentObj.put("exam", assignment.getExamId());
                assignmentObj.put("room", assignment.getRoom());
                assignmentObj.put("timeslot", assignment.getTimeslot());
                assignmentObj.put("students", assignment.getStudentCount());
                scheduleArray.put(assignmentObj);
            }

            output.put("schedule", scheduleArray);
            output.put("fitness", fitness);

            // 3. Ghi file với định dạng UTF-8
            String jsonString = output.toString(2);
            Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8));
            
            System.out.println("[Main] Success! Output written to: " + fileName);
            
        } catch (Exception e) {
            System.err.println("[Main] Error writing output: " + e.getMessage());
            e.printStackTrace();
        }
    }
}