package net.shadowmage.ancientwarfarerecompiled.core.util;

import net.shadowmage.ancientwarfarerecompiled.AncientWarfareRecompiled;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FileUtils {
	private FileUtils() {}

	public static void findFiles(File source, String base, @Nullable BiConsumer<Path, Path> processor) {
		findFiles(source, base, null, processor);
	}

	public static void findFiles(File source, String base, @Nullable Function<Path, Boolean> preprocessor, @Nullable BiConsumer<Path, Path> processor) {
		FileSystem fs = null;
		try {
			@Nullable Path root = null;
			if (source.isFile()) {
				try {
					fs = FileSystems.newFileSystem(source.toPath(), (ClassLoader) null);
					root = fs.getPath("/" + base);
				}
				catch (IOException e) {
					AncientWarfareRecompiled.LOGGER.error("Erreur lors du chargement du FileSystem depuis le jar : ", e);
					return;
				}
			} else if (source.isDirectory()) {
				root = source.toPath().resolve(base);
			}

			if (root == null || !Files.exists(root)) {
				return;
			}

			if (preprocessor != null) {
				Boolean cont = preprocessor.apply(root);
				if (cont == null || !cont) {
					return;
				}
			}

			if (processor != null) {
				Iterator<Path> itr;
				try {
					itr = Files.walk(root).iterator();
				}
				catch (IOException e) {
					AncientWarfareRecompiled.LOGGER.error("Erreur lors du parcours de {}", root, e);
					return;
				}

				while (itr.hasNext()) {
					processor.accept(root, itr.next());
				}
			}
		}
		finally {
			IOUtils.closeQuietly(fs);
		}
	}

	public static void exportToFile(File exportFile, List<String> rows) {
		if (!exportFile.exists()) {
			if (createFile(exportFile)) {
				return;
			}
		}
		try (FileWriter fileWriter = new FileWriter(exportFile); BufferedWriter writer = new BufferedWriter(fileWriter)) {
			for (String row : rows) {
				writer.write(row);
				writer.newLine();
			}
		}
		catch (IOException e) {
			AncientWarfareRecompiled.LOGGER.error("Erreur lors de l'export du fichier : " + exportFile.getAbsolutePath(), e);
		}
	}

	private static boolean createFile(File exportFile) {
		try {
			if (!exportFile.getParentFile().exists() && !exportFile.getParentFile().mkdirs()) {
				AncientWarfareRecompiled.LOGGER.error("Impossible de créer les dossiers pour : " + exportFile.getAbsolutePath());
				return true;
			}
			if (!exportFile.createNewFile()) {
				AncientWarfareRecompiled.LOGGER.error("Impossible d'ouvrir le nouveau fichier : " + exportFile.getAbsolutePath());
				return true;
			}
		}
		catch (IOException e) {
			AncientWarfareRecompiled.LOGGER.error("Erreur lors de l'ouverture du fichier : " + exportFile.getAbsolutePath(), e);
			return true;
		}
		return false;
	}
}
