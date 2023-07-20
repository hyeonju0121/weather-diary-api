package zerobase.weather.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemoRepositoryTest {

    @Autowired
    MemoRepository memoRepository;

    @Test
    void insertMemoTest() {
        //given
        Memo memo = new Memo(10, "this is first memo.");
        //when
        memoRepository.save(memo);
        //then
        List<Memo> memoList = memoRepository.findAll();
        assertTrue(memoList.size() > 0);
    }

    @Test
    void findByIdTest() {
        //given
        Memo newMemo = new Memo(11, "jpa");
        //when
        Memo memo = memoRepository.save(newMemo);
        System.out.println(memo.getId());
        //then
        Optional<Memo> result = memoRepository.findById(memo.getId());
        assertEquals(result.get().getText(), "jpa");
        System.out.println(result.get().getId());
    }
}