package cn.bounter.zip.controller;

import cn.bounter.zip.model.ExcelModel;
import com.alibaba.excel.EasyExcel;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/bounter")
public class ZipController {

    @GetMapping("/zip")
    public void zip(HttpServletResponse response, String secret) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();

        //设置文件名称
        String fileName = "bounter_" + System.currentTimeMillis();

        //设置压缩参数（加密）
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        //设置下载头
        response.setContentType("application/form-data");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".zip");

        //压缩打包并加密
        byte[] buff = new byte[4096];
        int readLen;
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, secret.toCharArray());
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            //生成excel文件，放入字节流
            EasyExcel.write(baos, ExcelModel.class).sheet("模板").doWrite(data());

            //设置解压后的每个文件的名称
            zipParameters.setFileNameInZip(String.format("%s.xlsx", fileName));
            //添加压缩参数
            zipOutputStream.putNextEntry(zipParameters);
            //从excel字节流写人压缩文件流
            try (InputStream inputStream = new ByteArrayInputStream(baos.toByteArray())) {
                while ((readLen = inputStream.read(buff)) != -1) {
                    zipOutputStream.write(buff, 0, readLen);
                }
            }
            zipOutputStream.closeEntry();
        }
    }

    private List<ExcelModel> data() {
        List<ExcelModel> list = new ArrayList<ExcelModel>();
        for (int i = 0; i < 10; i++) {
            ExcelModel data = new ExcelModel();
            data.setString("字符串" + i);
            data.setDate(new Date());
            data.setDoubleData(0.56);
            list.add(data);
        }
        return list;
    }
}
