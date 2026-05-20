# AeroLaser — Addon para Create Aeronautics

Mod addon para **NeoForge 1.21.1** que adiciona o bloco **Laser de Show** (`aerolaser:show_laser`),  
com configurações avançadas de laser estilo show/concerto, funciona junto com o Create Aeronautics 1.2.1+.

---

## ✨ Funcionalidades

| Configuração | Valores | Descrição |
|---|---|---|
| **Zoom** | 1 – 20 | 1 = raio fino, 20 = feixe largo |
| **Cor (R/G/B)** | 0 – 255 | Qualquer cor RGB |
| **Range** | 1 – 64 blocos | Comprimento do feixe |
| **Velocidade** | 1 – 20 | Velocidade das animações |
| **Modo** | 5 modos | Veja abaixo |

### Modos de operação
- **STATIC** — feixe fixo apontando para a direção do bloco
- **SWEEP** — varre 45° para cada lado (balanço)
- **SPIN** — gira 360° continuamente
- **BOUNCE** — quica de 0° a 90°
- **PULSE** — pulsa o zoom de 0 ao máximo configurado

### Como usar
1. Coloque o bloco **Laser de Show** apontando para a direção desejada
2. Clique nele para abrir o menu de configuração
3. Ajuste zoom, cor, velocidade, range e modo
4. Conecte redstone para ativar/desativar o laser

---

## 🔧 Como compilar (usando GitHub — sem PC)

### Passo 1 — Criar repositório no GitHub
1. Abra o GitHub no celular / navegador
2. Crie um novo repositório (ex: `aerolaser-mod`)
3. Habilite **GitHub Actions** (já está habilitado por padrão)

### Passo 2 — Fazer upload dos arquivos
Você pode usar o editor web do GitHub:
1. No repositório, clique em **Add file > Upload files**
2. Faça upload de toda a pasta do projeto **mantendo a estrutura de pastas**

> Dica: compacte a pasta em `.zip`, extraia no GitHub Web ou use o app **GitHub Mobile**.

### Passo 3 — Compilar automaticamente
1. Após o upload, vá em **Actions** no repositório
2. O workflow **"Build AeroLaser Mod"** vai iniciar automaticamente
3. Aguarde ~5–10 minutos para compilar
4. Baixe o `.jar` em **Actions > seu workflow > Artifacts > aerolaser-mod**

### Passo 4 — Instalar no ZalithLauncher
1. Baixe o `.jar` gerado
2. No ZalithLauncher: vá em **Mods > Adicionar mod**
3. Certifique-se de ter instalado também:
   - NeoForge 1.21.1
   - Create (6.x para 1.21.1)
   - Sable (dependência do Create Aeronautics)
   - Create Aeronautics 1.2.1

---

## 📁 Estrutura do projeto

```
aerolaser-addon/
├── build.gradle
├── gradle.properties       ← versão do mod aqui
├── settings.gradle
├── .github/workflows/
│   └── build.yml           ← GitHub Actions
└── src/main/
    ├── java/dev/aerolaser/
    │   ├── AeroLaserMod.java
    │   ├── block/
    │   │   ├── ShowLaserBlock.java
    │   │   └── ShowLaserMenu.java
    │   ├── blockentity/
    │   │   └── ShowLaserBlockEntity.java
    │   ├── client/
    │   │   ├── ClientEvents.java
    │   │   ├── gui/ShowLaserScreen.java
    │   │   └── renderer/ShowLaserRenderer.java
    │   ├── network/
    │   │   ├── AeroLaserNetwork.java
    │   │   └── LaserConfigPacket.java
    │   └── registry/
    │       ├── AeroLaserBlocks.java
    │       ├── AeroLaserBlockEntities.java
    │       ├── AeroLaserItems.java
    │       └── AeroLaserMenuTypes.java
    └── resources/
        ├── META-INF/neoforge.mods.toml
        └── assets/aerolaser/
            ├── blockstates/show_laser.json
            ├── lang/en_us.json
            ├── lang/pt_br.json
            └── models/...
```

---

## ⚠️ Texturas

As texturas precisam ser criadas por você (arquivos PNG 16×16):
- `src/main/resources/assets/aerolaser/textures/block/show_laser_side.png`
- `src/main/resources/assets/aerolaser/textures/block/show_laser_top.png`
- `src/main/resources/assets/aerolaser/textures/block/show_laser_front.png`
- `src/main/resources/assets/aerolaser/textures/block/show_laser_front_on.png`

Você pode usar qualquer editor de pixel art no celular (ex: **Dotpict**, **PixelStudio**).  
Sem as texturas o bloco aparece como quadrado rosa/preto (missing texture) mas funciona normalmente.

---

## 📝 Alterar seu nome de autor

Em `gradle.properties`, mude a linha:
```
mod_authors=YourName
```

---

## Licença
MIT — pode usar, modificar e redistribuir livremente.
