package com.dianping.ssp.crawler.list.test;

import com.dianping.ssp.crawler.common.util.ImgUploadUtil;
import com.dianping.ssp.file.download.SSPDownload;
import com.dianping.ssp.file.upload.SSPUpload;
import com.dianping.ssp.file.upload.client.venus.result.UploadResult;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by iClaod on 12/29/16.
 */
public class ImageUploadTest {

    private static String input_path = "htmlInput.txt";

    @Test
    public void test() throws Exception {
        Resource resource = new ClassPathResource(input_path);
        List<String> lines = Lists.newArrayList();
        if (resource.isReadable()) {
            lines = IOUtils.readLines(resource.getInputStream());
        }
        int i = 0;
        for (String line : lines) {
            i ++ ;
            System.out.println("process line " + i);
            int j = 0;
            String content = SSPDownload.MssS3.downloadPlainFileContent(line);
            Document document = Jsoup.parse(content);
            Elements elements = document.body().getElementsByTag("img");
            for (Element element : elements) {
                j ++ ;
                //System.out.println("process element " + j);
                String imgUrl = null;
                if (StringUtils.isNotBlank(element.attr("data-src"))) {
                    //wechat
                    imgUrl = element.attr("data-src").split("\\?")[0];
                } else if (StringUtils.isNotBlank(element.attr("src"))) {
                    imgUrl = element.attr("src");
                }
                if (imgUrl != null) {
                    try {
                        imgUrl = imgUrl.replaceAll("https://", "http://");
                        byte[] bytes = ImgUploadUtil.downloadImg(imgUrl);
                        BufferedImage sourceImg = ImageIO.read(new ByteArrayInputStream(bytes));
                        String mtRemoteAddr;
                        if (StringUtils.isNotBlank(element.attr("data-type"))) {
                            mtRemoteAddr = upload(bytes, "a." + element.attr("data-type"));
                        } else {
                            mtRemoteAddr =upload(bytes, imgUrl);
                        }
                        if (mtRemoteAddr == null) {
                            System.out.println("error upload" + element.outerHtml());
                        } else {
                            byte[] mtbytes = ImgUploadUtil.downloadImg(mtRemoteAddr);
                            BufferedImage mtSourceImg = ImageIO.read(new ByteArrayInputStream(mtbytes));
                            if (mtSourceImg.getHeight() != sourceImg.getHeight() || mtSourceImg.getWidth()!= sourceImg.getWidth()) {
                                System.out.println("[check error], originHeight: " + sourceImg.getHeight() + "originWidth: " + sourceImg.getWidth() + ", mtHeight: " + mtSourceImg.getHeight() + ", mtWidth: " + mtSourceImg.getWidth());
                                System.out.println("[url detail]remoteAddress: " + imgUrl + " ,mtAddress: " + mtRemoteAddr);
                            } else {
//                                System.out.println("[check success]" + imgUrl);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("error, imgUrl: " + imgUrl);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("pattern not found. " + element.outerHtml());
                }

            }
        }
        System.out.println(lines.size());
    }

    private String upload(byte[] picBytes, String fileName) {
        for (int i = 0 ; i< 3 ; i++) {
            UploadResult result = SSPUpload.Venus.getImageUploadClient().uploadFile(picBytes, fileName, null, "100000", "100000", null, "0");
            if (result == null || !result.isSuccess() || result.getData() == null || StringUtils.isEmpty(result.getData().getOriginalLink())) {
                continue;
            }
            return result.getData().getOriginalLink();
        }
        return null;
    }
}
