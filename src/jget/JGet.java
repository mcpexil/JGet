/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jget;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author Eliza Bland
 */
public class JGet {
    
    public static byte[] unbox(ArrayList<Byte> arr) {
        byte[] ret = new byte[arr.size()];
        for(int i = 0; i < arr.size(); i++) {
            ret[i] = arr.get(i);
        }
        return ret;
    }
    
    public static int getSize(URL url) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return con.getContentLength();
    }
    
    public static InputStream getStream(URL url) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return con.getInputStream();
    }
    
    public static byte[] blockAndReadAll(InputStream is, int contentSize) throws Exception {
        if(contentSize > 0) System.out.println("Size: " + contentSize + " bytes");
        else System.out.println("Undeterminable size.");
        int i = 0;
        ArrayList<Byte> bytes = new ArrayList<>();
        int readByte;
        while((readByte = is.read()) != -1) {
            bytes.add((byte)readByte);
            i++;
            
            if(contentSize > 0) {
                if(i % ((int)(contentSize / 100)) == 0) {
                    System.out.println((i / (contentSize / 100)) + "%...");
                }
            } else {
                if(i < 5) System.out.println(i + " bytes...");
                if(i % 1024 == 0) System.out.println((i / 1024) + "KB...");
            }
        }
        return unbox(bytes);
    }
    
    public static byte[] blockAndReadAllBin(InputStream is, int contentSize) throws Exception {
        ArrayList<Byte> bytes = new ArrayList<>();
        int i = 0;
        int readByte;
        while((readByte = is.read()) != -1) {
            bytes.add((byte)readByte);
            i++;
            
            if(contentSize != 0) {
                if(i % ((int)(contentSize / 100)) == 0) {
                    System.out.println((i / (contentSize / 100)) + "%...");
                }
            }
        }
        return unbox(bytes);
    }
    
    public static InputStream getStream(String link) throws Exception {
        return getStream(new URL(link));
    }
    
    public static void saveImage(String URLStr, String path) throws Exception {
        InputStream is = getStream(URLStr);
        BufferedImage bi = ImageIO.read(is);
        boolean isWrite = ImageIO.write(bi, ".jpg", new File(path));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        if(args.length == 0) {
            System.out.println("Usage: JGet [--use-html {path}] [--url {url}] [--use-proxy {ip} {port}] [--save-html]");
            System.exit(0);
        }
        String htmlFile = "";
        String urlStr = "";
        String proxyIp = "";
        String proxyPort = "";
        boolean isSaveHtml = false;
        boolean isSaveImages = true;
        boolean isSaveParsedLinks = false;
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "--use-html":
                    htmlFile = args[++i];
                    break;
                    
                case "--url":
                    urlStr = args[++i];
                    break;
                    
                case "--use-proxy":
                    proxyIp = args[++i];
                    proxyPort = args[++i];
                    break;
                    
                case "--save-html":
                    isSaveHtml = true;
                    break;
                    
                case "--no-download-images":
                    isSaveImages = false;
                    break;
                    
                case "--save-parsed-links":
                    isSaveParsedLinks = true;
                    break;
            }
        }
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line, data = "", path = "";
        
        byte[] dataBuffer = null;
        
        String board = "", thread = "";
        
        if(!urlStr.equals("")) {
            url = new URL(urlStr);
            board  = urlStr.split("/")[3];
            thread = urlStr.split("/")[5].split("\\.")[0];
            path = System.getProperty("user.home") + "\\documents\\8chan-" + board + "-" + thread;
            if(!proxyIp.equals("")) {
                System.setProperty("https.proxyHost", args[1]);
                System.setProperty("https.proxyPort", args[2]);
                System.out.println("Using proxy " + args[1] + ":" + args[2]);
            }
            System.out.print("Requesting connection... ");
            dataBuffer = blockAndReadAll(getStream(url), getSize(url));
        } else if (!htmlFile.equals("")) {
            File f = new File(htmlFile);
            board  = f.getName().split("-")[0];
            thread = f.getName().split("-")[1].split("\\.")[0];
            path = System.getProperty("user.home") + "\\documents\\8chan-" + board + "-" + thread;
            FileInputStream fis = new FileInputStream(f);
            dataBuffer = new byte[fis.available()];
            fis.read(dataBuffer, 0, dataBuffer.length);
            fis.close();
        }
        
        new File(path).mkdir();
        
        if(isSaveHtml) {
            FileOutputStream fos = new FileOutputStream(new File(path + "\\" + board + "-" + thread + ".html"));
            fos.write(dataBuffer);
            fos.close();
        }
        
        data = new String(dataBuffer);
        // <a> tag pass
        if(isSaveParsedLinks || isSaveImages) {
            System.out.println("Finding <a> tags...");
            ArrayList<String> tags = new ArrayList<>();

            int start = 0, end;

            while(data.substring(start).contains("<")) {

                end = data.substring(start).indexOf(">") + start + 1;
                tags.add(data.substring(start, end));
                start = end;
            }
            System.out.println("Filtering tags...");
            ArrayList<String> aTags = new ArrayList<>();
            for(int i = 0; i < tags.size(); i++) {
                String s = tags.get(i);
                if((s.contains("<a"))) {
                    aTags.add(s.substring(s.indexOf("<")));
                }
            }
            System.out.println(aTags.size() + " <a> tags found.");

            // href pass
            System.out.println("Finding hrefs...");
            ArrayList<String> hrefs = new ArrayList<>();

            Pattern hrefPattern = Pattern.compile("href=\"(.*?)\"");
            for(String s : aTags) {
                Matcher m = hrefPattern.matcher(s);
                if(m.find()) {
                    int startlcl = s.indexOf("href=") + 6;
                    String sub = s.substring(startlcl);
                    int endlcl = sub.indexOf("\"") + startlcl;
                    hrefs.add(s.substring(startlcl, endlcl));
                }
            }

            System.out.println(hrefs.size() + " hrefs found.");


            // jpg pass
            System.out.println("Finding images...");
            ArrayList<String> jpgs = new ArrayList<>();

            for(String s : hrefs) {
                if((s.contains(".jpg") || s.contains(".jpeg") || s.contains(".png") || s.contains(".gif")) && !jpgs.contains(s)) jpgs.add(s);
            }

            System.out.println(jpgs.size() + " images found.");
            
            if(isSaveParsedLinks) {
                PrintWriter pw = new PrintWriter(new FileOutputStream(new File(path + "\\parsed-links.txt")));
                jpgs.stream().forEach((s) -> {
                    pw.write(s + "\n");
                });
                
                pw.flush();
                pw.close();
            }
            
            if(isSaveImages) {
                for(String s : jpgs) {
                    URL imgUrl = new URL(s);
                    String name = s.split("/")[s.split("/").length-1];
                    System.out.println("Retrieving " + name + "...");
                    byte[] imgBuf = blockAndReadAllBin(getStream(imgUrl), 0);
                    FileOutputStream fos = new FileOutputStream(path + "\\" + name);
                    fos.write(imgBuf);
                    fos.close();
                }
            
                long endTime = System.nanoTime();
                System.out.println("Retrieved " + jpgs.size() + " images in " + ((int)((endTime - startTime) / 1000000)) + "ms");
            }
        }
    }
}
