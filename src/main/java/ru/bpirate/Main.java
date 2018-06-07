package ru.bpirate;

import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import java.awt.*;
import java.io.File;
import java.util.List;

public class Main {
    static String pathToParentFolder; //in windows format, e.g. C:\Users\Bpirate\Desktop\folder-with-images
    static List<File> targetImages;

    public static void main(String[] args) throws BackendException, FileException {
        //Looking up path to running .jar
        pathToParentFolder = FileService.findParentFolder();

        //Listing images
        targetImages = FileService.listImages(pathToParentFolder);

        //Checking if all images have same extension
        if (!FileService.checkExtension(targetImages)) {
            throw new FileException("Not all images have same format!", new Exception());
        }

        //Backup
        FileService.backupFiles(pathToParentFolder, targetImages);

        //Unpack FFMPEG
        ResourceService.exportResource("/ffmpeg.exe", pathToParentFolder);

        //Check if all files have same resolution
        if (!FileService.checkSizes(targetImages)) {
            System.out.println("Resizing images!");
            Dimension smallestDimension = FileService.findLowestDimension(targetImages);
            FFMPEGService.resizeFiles(targetImages, smallestDimension, pathToParentFolder);
        }







        /*
        План:
            * Определяем путь до папки.
            * Проверяем, что в папке есть изображения.
            * Достаем все изображения в папке.
            * Сортируем файлы в виндовом формате.
            * Делаем резервную копию всех файлов в отдельную папку. <-
            * Проверяем, что у всех изображений одинаковые размеры.
            * * Если нет, то выбираем наименьшее и форматируем остальные под него.
            * Распаковываем FFMPEG.
            * FFMPEG CMD.
            * Копируем имя первого файла в буфер обмена.
            * Трем исходные файлы.
         */

    }

}
