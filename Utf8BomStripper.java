import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class Utf8BomStripper {
    private static final byte[] UTF8_BOM = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};

    public static void main(String[] args) throws IOException {
        Path root = args.length >= 1 ? Paths.get(args[0]) : Paths.get("/src");
        boolean dryRun = hasFlag(args, "--dry-run");
        boolean noBackup = hasFlag(args, "--no-backup");

        if (!Files.exists(root)) {
            System.err.println("Path does not exist: " + root.toAbsolutePath());
            System.exit(2);
        }

        List<Path> javaFiles = new ArrayList<>();
        try (var walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".java"))
                .forEach(javaFiles::add);
        }

        int scanned = 0, changed = 0, errors = 0;

        for (Path p : javaFiles) {
            scanned++;
            try {
                byte[] bytes = Files.readAllBytes(p);
                if (hasUtf8Bom(bytes)) {
                    changed++;
                    System.out.println("BOM -> stripped: " + p.toAbsolutePath());

                    if (!dryRun) {
                        if (!noBackup) {
                            Path bak = backupPath(p);
                            Files.copy(p, bak, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                        byte[] stripped = new byte[bytes.length - UTF8_BOM.length];
                        System.arraycopy(bytes, UTF8_BOM.length, stripped, 0, stripped.length);
                        Files.write(p, stripped, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    }
                }
            } catch (Exception e) {
                errors++;
                System.err.println("ERROR: " + p.toAbsolutePath() + " -> " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("Done.");
        System.out.println("Scanned : " + scanned);
        System.out.println("Changed : " + changed + (dryRun ? " (dry-run)" : ""));
        System.out.println("Errors  : " + errors);
    }

    private static boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3
            && bytes[0] == UTF8_BOM[0]
            && bytes[1] == UTF8_BOM[1]
            && bytes[2] == UTF8_BOM[2];
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) if (a.equals(flag)) return true;
        return false;
    }

    private static Path backupPath(Path original) {
        // Keep backups alongside original: Foo.java -> Foo.java.bak
        return original.resolveSibling(original.getFileName().toString() + ".bak");
    }
}
