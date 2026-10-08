package com.xz.springboot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisualizationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void rendersAllNegativeAndConstantTimeSlicesAndRejectsInvalidIndices() throws Exception {
        Path ncPath = tempDir.resolve("sst-test.nc");
        createNetcdf(ncPath);

        VisualizationService service = new VisualizationService();
        Field outputDir = VisualizationService.class.getDeclaredField("plotOutputDir");
        outputDir.setAccessible(true);
        outputDir.set(service, tempDir.toString());

        VisualizationService.PlotResult negative = service.createPlotWithStats(ncPath.toFile(), "sst", 0);
        assertEquals(-4f, negative.getMin());
        assertEquals(-1f, negative.getMax());
        assertEquals(-2.5f, negative.getMean());
        assertEquals(4, negative.getValidCount());
        BufferedImage negativeImage = readGeneratedImage(negative.getImageUrl());
        assertNotNull(negativeImage);
        assertEquals(4, negativeImage.getWidth() * negativeImage.getHeight());

        VisualizationService.PlotResult constant = service.createPlotWithStats(ncPath.toFile(), "sst", 1);
        assertEquals(-2f, constant.getMin());
        assertEquals(-2f, constant.getMax());
        BufferedImage constantImage = readGeneratedImage(constant.getImageUrl());
        assertNotNull(constantImage);
        assertEquals(constantImage.getRGB(0, 0), constantImage.getRGB(1, 1));

        assertThrows(IOException.class, () -> service.createPlotWithStats(ncPath.toFile(), "sst", -1));
        assertThrows(IOException.class, () -> service.createPlotWithStats(ncPath.toFile(), "sst", 2));
    }

    private BufferedImage readGeneratedImage(String imageUrl) throws IOException {
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        return ImageIO.read(tempDir.resolve(filename).toFile());
    }

    private void createNetcdf(Path path) throws Exception {
        NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, path.toString());
        Dimension time = writer.addDimension(null, "time", 2);
        Dimension lat = writer.addDimension(null, "lat", 2);
        Dimension lon = writer.addDimension(null, "lon", 2);
        Variable variable = writer.addVariable(null, "sst", DataType.FLOAT, Arrays.asList(time, lat, lon));
        writer.create();
        writer.write(variable, Array.factory(DataType.FLOAT, new int[]{2, 2, 2}, new float[]{
                -4f, -3f, -2f, -1f,
                -2f, -2f, -2f, -2f
        }));
        writer.close();
    }
}
