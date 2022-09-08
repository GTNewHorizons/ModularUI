package com.gtnewhorizons.modularui.api.forge;


import com.gtnewhorizons.modularui.ModularUI;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.item.crafting.CraftingManager;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CraftingHelper {

    private static final boolean DEBUG_LOAD_MINECRAFT = false;

    public static boolean findFiles(ModContainer mod, String base, Function<Path, Boolean> preprocessor, BiFunction<Path, Path, Boolean> processor,
                                    boolean defaultUnfoundRoot, boolean visitAllFiles) {

        File source = mod.getSource();

        if ("minecraft".equals(mod.getModId()))
        {
            if (!DEBUG_LOAD_MINECRAFT)
                return true;

            try
            {
                URI tmp = CraftingManager.class.getResource("/assets/.mcassetsroot").toURI();
                source = new File(tmp.resolve("..").getPath());
            }
            catch (URISyntaxException e)
            {
                ModularUI.logger.error("Error finding Minecraft jar: ", e);
                return false;
            }
        }

        FileSystem fs = null;
        boolean success = true;

        try
        {
            Path root = null;

            if (source.isFile())
            {
                try
                {
                    fs = FileSystems.newFileSystem(source.toPath(), null);
                    root = fs.getPath("/" + base);
                }
                catch (IOException e)
                {
                    ModularUI.logger.error("Error loading FileSystem from jar: ", e);
                    return false;
                }
            }
            else if (source.isDirectory())
            {
                root = source.toPath().resolve(base);
            }

            if (root == null || !Files.exists(root))
                return defaultUnfoundRoot;

            if (preprocessor != null)
            {
                Boolean cont = preprocessor.apply(root);
                if (cont == null || !cont.booleanValue())
                    return false;
            }

            if (processor != null)
            {
                Iterator<Path> itr = null;
                try
                {
                    itr = Files.walk(root).iterator();
                }
                catch (IOException e)
                {
                    ModularUI.logger.error("Error iterating filesystem for: {}", mod.getModId(), e);
                    return false;
                }

                while (itr != null && itr.hasNext())
                {
                    Boolean cont = processor.apply(root, itr.next());

                    if (visitAllFiles)
                    {
                        success &= cont != null && cont;
                    }
                    else if (cont == null || !cont)
                    {
                        return false;
                    }
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(fs);
        }

        return success;
    }
}
