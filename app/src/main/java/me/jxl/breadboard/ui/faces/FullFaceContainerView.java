package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.ui.Theme;

/**
 * 1:1 Apple HomeView Full Screen Face View
 * - Displays active watch face edge-to-edge
 * - Tapping anywhere on the face initiates the Recognizing flow
 * - Subtle frosted capsule in top-left: "⊞ Galerie" to return to Faces Gallery
 */
public class FullFaceContainerView extends FrameLayout {

    public interface OnFaceInteractionListener {
        void onTapToRecognize();
        void onBackToGallery();
        void onOpenMusic();
    }

    private final float density;
    private OnFaceInteractionListener listener;

    private FrameLayout faceContainer;
    private View currentFaceView;
    private FaceThumbnailView.FaceKind currentKind = FaceThumbnailView.FaceKind.DAD_SON;

    public FullFaceContainerView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnFaceInteractionListener(OnFaceInteractionListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        setBackgroundColor(Color.BLACK);

        // 1. Face Container (fills screen)
        faceContainer = new FrameLayout(context);
        addView(faceContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Load Default Face: Dad & Son
        setFace(context, FaceThumbnailView.FaceKind.DAD_SON);

        // 2. Top-Left Subtle Gallery Return Pill ("⊞ Galerie")
        LinearLayout galleryBtn = new LinearLayout(context);
        galleryBtn.setOrientation(LinearLayout.HORIZONTAL);
        galleryBtn.setGravity(Gravity.CENTER_VERTICAL);
        galleryBtn.setPadding(Theme.dp(14, density), Theme.dp(7, density), Theme.dp(14, density), Theme.dp(7, density));
        galleryBtn.setBackground(Theme.makePill(0x33000000, 0x4DFFFFFF, density));

        TextView tvIcon = new TextView(context);
        tvIcon.setText("⊞");
        tvIcon.setTextColor(0xD9FFFFFF);
        tvIcon.setTextSize(14);
        tvIcon.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        galleryBtn.addView(tvIcon);

        TextView tvLabel = new TextView(context);
        tvLabel.setText(" Galerie");
        tvLabel.setTextColor(0xD9FFFFFF);
        tvLabel.setTextSize(13);
        tvLabel.setTypeface(Theme.getBoldTypeface(context));
        galleryBtn.addView(tvLabel);

        LayoutParams lpBtn = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpBtn.leftMargin = Theme.dp(24, density);
        lpBtn.topMargin = Theme.dp(20, density);
        galleryBtn.setLayoutParams(lpBtn);

        galleryBtn.setOnClickListener(v -> {
            galleryBtn.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).withEndAction(() -> {
                galleryBtn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                if (listener != null) listener.onBackToGallery();
            }).start();
        });

        addView(galleryBtn);
    }

    public void setFace(Context context, FaceThumbnailView.FaceKind kind) {
        this.currentKind = kind;
        faceContainer.removeAllViews();

        switch (kind) {
            case MODULAR_QUAD: {
                ModularFaceView modular = new ModularFaceView(context);
                modular.setOnMusicWidgetClickListener(() -> {
                    if (listener != null) listener.onOpenMusic();
                });
                modular.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = modular;
                break;
            }
            case SMART_HOME_INDIGO: {
                SmartHomeFaceView smart = new SmartHomeFaceView(context);
                smart.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = smart;
                break;
            }
            case SNOOPY: {
                SnoopyFaceView snoopy = new SnoopyFaceView(context);
                snoopy.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = snoopy;
                break;
            }
            case CALENDAR_MATRIX: {
                CalendarFaceView cal = new CalendarFaceView(context);
                cal.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = cal;
                break;
            }
            case TYPOGRAPHIC_SAGE: {
                TypographicFaceView typo = new TypographicFaceView(context);
                typo.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = typo;
                break;
            }
            case FLOW_JELLY: {
                FlowFaceView flow = new FlowFaceView(context);
                flow.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = flow;
                break;
            }
            case STANDBY: {
                StandByFaceView standBy = new StandByFaceView(context);
                standBy.setOnClickListener(v -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = standBy;
                break;
            }
            case DAD_SON:
            default: {
                DadSonFaceView dadSon = new DadSonFaceView(context);
                dadSon.setOnRecognizeTriggerListener(() -> {
                    if (listener != null) listener.onTapToRecognize();
                });
                currentFaceView = dadSon;
                break;
            }
        }

        faceContainer.addView(currentFaceView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }
}
