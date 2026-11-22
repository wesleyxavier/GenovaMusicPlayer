# Research & Technical Decisions: MVP Parte 1 — GenovaMusicPlayer

**Feature**: `001-mvp-part1-player` | **Date**: 2026-11-21

## 1. Lista Virtualizada sem AndroidX / RecyclerView

- **Decision**: Utilizar `android.widget.ListView` com `BaseAdapter` e padrão `ViewHolder` explícito, mantendo apenas os itens visíveis instanciados e reciclados.
- **Rationale**: A Constituição (Princípio I) proíbe AndroidX e `RecyclerView`. `ListView` faz parte do framework Android nativo (`android.widget`), virtualiza views nativamente via reciclagem de `convertView` e atende ao requisito de abertura rápida com 2.000+ faixas quando combinado com carregamento assíncrono de capas.
- **Alternatives considered**: `ScrollView` com `LinearLayout` (rejeitado por instanciar todas as 2.000+ views na abertura e violar virtualização); implementação manual de `ViewGroup` reciclável (complexidade desnecessária frente ao `ListView` nativo).

## 2. Seleção de Pastas (`ACTION_OPEN_DOCUMENT_TREE`) e Filtro no `MediaStore`

- **Decision**: Usar `Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)` com `takePersistableUriPermission(..., FLAG_GRANT_READ_URI_PERMISSION)`, extrair o caminho normalizado de volume/subpasta a partir do `treeUri` (ex.: `primary:Music` → `/storage/emulated/0/Music`) e filtrar consultas ao `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` usando `MediaStore.Audio.Media.DATA` (ou `RELATIVE_PATH` em API 29+ combinado com `VOLUME_NAME`) comparado contra os prefixos de segmentos completos das pastas autorizadas.
- **Rationale**: Evita varredura recursiva manual via `DocumentFile` (que é lenta para 2.000+ arquivos) mantendo a conformidade com a Constituição (§3: seleção via `ACTION_OPEN_DOCUMENT_TREE` + leitura filtrada via `MediaStore`).
- **Alternatives considered**: `DocumentFile.listFiles()` recursivo (muito lento em armazenamento compartilhado para 2.000+ músicas); `MANAGE_EXTERNAL_STORAGE` (proibido pelo escopo restrito às pastas selecionadas).

## 3. Regra de Hierarquia e Normalização em Kotlin Puro

- **Decision**: Implementar `FolderPathNormalizer` e `FolderHierarchyValidator` em módulo/pacote Kotlin puro (`domain/folder/`), sem imports `android.*`. Caminhos são normalizados (remoção de espaços extras, colapso de barras múltiplas, remoção de barra final exceto raiz, conversão consistente de separadores) e divididos em segmentos (`split('/')`) para checagem de ancestral/descendente.
- **Rationale**: Atende diretamente ao Princípio II e Princípio IV da Constituição, permitindo testes unitários JVM rápidos cobrindo os 7 casos obrigatórios (igual, filha, mãe, irmã, prefixo de texto `/Music` vs `/Music2`, barra final e caminho vazio).
- **Alternatives considered**: `java.nio.file.Path` com dependência de sistema de arquivos real (acoplado ao SO; comparação pura de segmentos em `String` é determinística e portável).

## 4. Carregamento Sob Demanda e Cache de Capas

- **Decision**: Usar `android.util.LruCache<String, Bitmap>` dimensionado por fração da memória disponível (`Runtime.getRuntime().maxMemory() / 8`), alimentado por um `ExecutorService` fixo (ex.: 2–4 threads) que decodifica a miniatura via `ContentResolver.openFileDescriptor` / `MediaMetadataRetriever` com `BitmapFactory.Options.inJustDecodeBounds` + `inSampleSize` para o tamanho alvo (`48dp` na lista, `320dp` em Tocando Agora), entregando o resultado à main thread via `Handler(Looper.getMainLooper())` somente se o `ViewHolder` ainda corresponder à mesma faixa (`tag`).
- **Rationale**: Zero bibliotecas de imagem (sem Glide/Coil), zero decodificação na main thread, uso controlado de RAM e eliminação de flickering durante rolagem rápida.
- **Alternatives considered**: `AsyncTask` (depreciado no Android); `ThumbnailUtils` síncrono (bloqueia main thread).

## 5. Máquina de Estados do `MediaPlayer`, `MediaSession` e Áudio em Segundo Plano

- **Decision**: Encapsular `android.media.MediaPlayer` em um controlador com enum de estado explícito (`IDLE`, `PREPARING`, `PLAYING`, `PAUSED`, `COMPLETED`, `ERROR`) dentro de um `ForegroundService` (`PlaybackService`), expor controles via `android.media.session.MediaSession`, notificação via `Notification.MediaStyle`, gerenciar foco com `AudioManager.requestAudioFocus` (`AudioFocusRequest` em API 26+) e escutar `AudioManager.ACTION_AUDIO_BECOMING_NOISY` via `BroadcastReceiver` para pausar ao desconectar fones.
- **Rationale**: Evita `IllegalStateException` do `MediaPlayer`, garante controles na tela bloqueada/notificação e cumpre todos os requisitos do Princípio V sem `Media3`.
- **Alternatives considered**: `MediaPlayer` direto na `Activity` (não sobrevive em segundo plano nem expõe controles de sistema).

## 6. Design System Nocturne Violet sem Bibliotecas UI

- **Decision**: Traduzir os tokens de `nocturne_violet/DESIGN.md` (`#13121B` surface, `#9D7BFF` primary-container, `#CEBDFF` primary, `#E5E0EE` on-surface, `#201E28` surface-container, raios `8dp`/`16dp`/pill, alvos `>= 48dp`) para recursos XML nativos (`res/values/colors.xml`, `res/values/dimens.xml`, `res/values/styles.xml`, `res/drawable/*.xml`) e layouts XML com `View`s do framework (`LinearLayout`, `FrameLayout`, `RelativeLayout`, `ListView`, `SeekBar`, `ImageButton`, `TextView`), incluindo a fonte `Plus Jakarta Sans` em `res/font/` (arquivos `.ttf` estáticos, carregáveis nativamente via `Resources.getFont()` sem AndroidX).
- **Rationale**: Fiel ao design de referência Stitch mantendo 100% de conformidade com "Views do framework, sem Compose e sem Material Components/AndroidX".
- **Alternatives considered**: `ConstraintLayout` (pertence ao AndroidX, portanto proibido).

## 7. Ambiente de Build e Testes (Docker Isolation)

- **Decision**: Executar todos os builds Gradle e testes unitários JVM dentro de container Docker com JDK + Android SDK (`compileSdk 34`, `minSdk 26`), sem instalação local de JDK/Android SDK no host Windows.
- **Rationale**: Cumpre a regra global de **Docker Isolation** para desenvolvimento Android/Kotlin.
- **Alternatives considered**: Instalação local de Android Studio/Gradle/JDK (proibida pela regra global).
